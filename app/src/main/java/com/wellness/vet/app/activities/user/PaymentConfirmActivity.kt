package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityPaymentConfirmBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.payments.client.ApiClient
import com.wellness.vet.app.payments.model.AmountTransferRequestBody
import com.wellness.vet.app.payments.model.FetchTokenModel
import com.wellness.vet.app.payments.model.FetchTransferAmountModel
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class PaymentConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmBinding
    private lateinit var doctorProfileDbRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var doctorUid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataBase = Firebase.database
        val appointDbRef = dataBase.getReference("appointments")
        doctorProfileDbRef = dataBase.getReference("Doctors")
        appSharedPreferences = AppSharedPreferences(this@PaymentConfirmActivity)
        doctorUid = intent.getStringExtra("doctorUid").toString()
        val userUid = intent.getStringExtra("userUid").toString()
        val slotDate = intent.getStringExtra("dateSlot").toString()
        val timeSlot = intent.getStringExtra("timeSlot").toString()

        var accountName: String = ""
        var accountNo: String = ""
        var accountFees: String = ""
        var preAmount: String = ""

        doctorProfileDbRef.child(doctorUid).child("Profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    accountName = snapshot.child("name").value.toString()
                    accountNo = snapshot.child("accountNumber").value.toString()
                    accountFees = snapshot.child("fees").value.toString()
                    binding.txtPrice.text = "Rs $accountFees/-"
                    binding.toAccountTitle.setText(accountName)
                    binding.toAccountNo.setText(accountNo)

                    for (i in 1..(12 - accountFees.length)) {
                        preAmount = "0$preAmount"
                    }

                    preAmount = "$preAmount$accountFees"

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        binding.confirmAppointment.setOnClickListener(View.OnClickListener {

            val cardHolder = binding.cardHolderName.text.toString().trim()
            val cardNo = binding.cardNumber.text.toString().replace("-", "").trim()
            val cardExpiryDate = binding.cardDate.text.toString().trim()
            val cardCVV = binding.cardCvc.text.toString().trim()
            Log.d("TAGCARD", "onCreate: $cardNo")
            if(cardNo != "3528555323354911"){
                binding.cardNumber.error = "Please Enter Valid Card Number"
                Toast.makeText(
                    this@PaymentConfirmActivity,
                    "Please Enter Valid Card Number",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            if(cardExpiryDate != "11/2030"){
                binding.cardDate.error = "Please Enter Valid Card Expiry"
                Toast.makeText(
                    this@PaymentConfirmActivity,
                    "Please Enter Valid Card Expiry",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            if(cardCVV != "021"){
                binding.cardCvc.error = "Please Enter Valid Card CVV"
                Toast.makeText(
                    this@PaymentConfirmActivity,
                    "Please Enter Valid Card CVC",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }


            val tokenCall = ApiClient.apiService.getAccessToken(
                "client_credentials",
                "1LinkApi",
                "82f0fbf555a1aa7c8a38662461ebcfa9",
                "029a10b09dd90e8bfe74b45e1956c916"
            )


            tokenCall.enqueue(object : Callback<FetchTokenModel> {

                override fun onResponse(
                    call: Call<FetchTokenModel>,
                    response: Response<FetchTokenModel>
                ) {
                    if (response.code() == 200) {
                        val transferCall = ApiClient.apiService.transferAmount(
                            "Bearer ${response.body()?.access_token}",
                            "82f0fbf555a1aa7c8a38662461ebcfa9",
                            createAmountTransferRequestBody(
                                accountName,
                                accountNo,
                                preAmount,
                                cardHolder,
                                cardNo,
                                cardExpiryDate,
                                cardCVV
                            )
                        )

                        transferCall.enqueue(object : Callback<FetchTransferAmountModel> {
                            override fun onResponse(
                                call: Call<FetchTransferAmountModel>,
                                response: Response<FetchTransferAmountModel>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()?.responseCode == "00") {
                                        val userDbRef = appointDbRef.child(userUid).child(doctorUid)
                                        val doctorDbRef =
                                            appointDbRef.child(doctorUid).child(userUid)
                                        val pushIdRef = userDbRef.push()
                                        val pushId = pushIdRef.key

                                        val mMap = HashMap<String, Any>()
                                        mMap["date"] = slotDate
                                        mMap["time"] = timeSlot
                                        mMap["appointmentStatus"] = "booked"

                                        userDbRef.child("$pushId").setValue(mMap)
                                            .addOnCompleteListener(OnCompleteListener { uAppoint ->
                                                if (uAppoint.isSuccessful) {
                                                    doctorDbRef.child("$pushId").setValue(mMap)
                                                        .addOnCompleteListener(OnCompleteListener { dAppoint ->
                                                            if (dAppoint.isSuccessful) {
                                                                val slotMap = HashMap<String, Any>()
                                                                slotMap["time"] = timeSlot
                                                                doctorProfileDbRef.child(doctorUid)
                                                                    .child("appointments")
                                                                    .child(slotDate)
                                                                    .child("$pushId")
                                                                    .setValue(slotMap)
                                                                    .addOnCompleteListener(
                                                                        OnCompleteListener { slotAppoint ->
                                                                            if (slotAppoint.isSuccessful) {
                                                                                getDoctorFCMToken(
                                                                                    doctorUid
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@PaymentConfirmActivity,
                                                                                    getString(R.string.successful_scheduled),
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                finish()

                                                                            }
                                                                        })
                                                            }
                                                        })
                                                }
                                            })
                                    } else {
                                        Toast.makeText(
                                            this@PaymentConfirmActivity,
                                            response.body()?.responseDetail,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<FetchTransferAmountModel>,
                                t: Throwable
                            ) {
                                Log.d("TAGLINK", "onResponse: ${t.message}")
                            }

                        })

                    }
                }

                override fun onFailure(call: Call<FetchTokenModel>, t: Throwable) {
                    Log.d("TAGLINK", "onResponse: ${t.message}")
                }

            })
        })

    }

    private fun createAmountTransferRequestBody(
        accountName: String, accountNo: String, amount: String,
        cardHolder: String, cardNo: String, cardDate: String, cardCVV: String
    ): AmountTransferRequestBody {
        val format = SimpleDateFormat("MMddhhmmss")
        val singleDateFormat = SimpleDateFormat("MMdd")
        val singleTimeFormat = SimpleDateFormat("hhmmss")
        format.timeZone = TimeZone.getDefault()
        singleDateFormat.timeZone = TimeZone.getDefault()
        singleTimeFormat.timeZone = TimeZone.getDefault()
        val currentDate = Date()
        val dateAndTime = format.format(currentDate)
        val date = singleDateFormat.format(currentDate)
        val time = singleTimeFormat.format(currentDate)
        val location = AmountTransferRequestBody.CardAcceptorNameLocation(
            "Park Towers",
            "Karachi",
            "Sindh",
            "34566",
            accountName,
            "Karachi",
            "Any Channel",
            "Allied",
            "PK"
        )
        return AmountTransferRequestBody(
            amount, dateAndTime, "102045", time, date, "0003",
            "998876", "000000024424",
            "102043", "40260275", "402626030259047",
            location, "", "Appointment Fees",
            "586", cardNo, accountNo,
            "221166", "4250108749566", "2812",
            cardCVV, "Allied Bank",
            "Reserved for future", "Reserved for future",
            "Reserved for future", "4226811024113664", "ajiwkeh",
            cardHolder, "DE89370400440532013000"
        )
    }

    private fun getDoctorFCMToken(doctorId: String) {
        doctorProfileDbRef.child(doctorId).child(AppConstants.PROFILE_REF).child("fcmToken").get()
            .addOnCompleteListener { task -> sendNotification(task.result.value.toString()) }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@PaymentConfirmActivity, e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotification(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", appSharedPreferences.getString("userName"))
                    put("body", "Scheduled an appointment.")
                    put("userType", "userAppointment")
                    put("uid", appSharedPreferences.getString("userUid"))
                    put("name", appSharedPreferences.getString("userName"))
                    put("imgUrl", appSharedPreferences.getString("userImgUrl"))
                }
                put("data", dataObj)
                put("to", token)
            }
            callApi(jsonObject)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    private fun callApi(jsonObject: JSONObject) {
        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder().url(url).post(body).header(
            "Authorization", getString(R.string.bearer_token)
        ).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle failure
                this@PaymentConfirmActivity.runOnUiThread {
                    Toast.makeText(this@PaymentConfirmActivity, e.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

            }
        })
    }
}