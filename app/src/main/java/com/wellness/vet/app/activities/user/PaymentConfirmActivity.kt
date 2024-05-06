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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.databinding.ActivityPaymentConfirmBinding
import com.wellness.vet.app.payments.client.ApiClient
import com.wellness.vet.app.payments.model.AmountTransferRequestBody
import com.wellness.vet.app.payments.model.FetchTokenModel
import com.wellness.vet.app.payments.model.FetchTransferAmountModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class PaymentConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataBase = Firebase.database
        val appointDbRef = dataBase.getReference("appointments")
        val doctorProfileDbRef = dataBase.getReference("Doctors")

        val userUid = intent.getStringExtra("userUid").toString()
        val doctorUid = intent.getStringExtra("doctorUid").toString()
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
                    "Please Enter Valid Card CVV",
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

                                                                                Toast.makeText(
                                                                                    this@PaymentConfirmActivity,
                                                                                    "Successful Scheduled",
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
}