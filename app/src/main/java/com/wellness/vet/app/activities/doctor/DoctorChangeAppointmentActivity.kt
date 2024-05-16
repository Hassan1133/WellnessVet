package com.wellness.vet.app.activities.doctor

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.TimeSlotAdapter
import com.wellness.vet.app.databinding.ActivityDoctorChangeAppointmentBinding
import com.wellness.vet.app.interfaces.TimeSlotSelectListener
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.models.DoctorAppointmentListModel
import com.wellness.vet.app.models.TimeSlotModel
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DoctorChangeAppointmentActivity : AppCompatActivity(), TimeSlotSelectListener {
    private lateinit var binding: ActivityDoctorChangeAppointmentBinding
    private val calendar = Calendar.getInstance()
    private var slotSelected = false
    private lateinit var model: DoctorAppointmentListModel
    private lateinit var timeSlot: String
    private lateinit var slotDate: String
    private var userId = ""
    private var doctorUid = ""
    private lateinit var appointDbRef: DatabaseReference
    private lateinit var doctorProfileDbRef: DatabaseReference
    private lateinit var userProfileDbRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorChangeAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        getDataFromIntent()
        appSharedPreferences = AppSharedPreferences(this@DoctorChangeAppointmentActivity)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        val todayDate = dateFormat.format(currentTime)
        binding.slotDate.setText(todayDate.toString())

        val dataBase = Firebase.database
        appointDbRef = dataBase.getReference("appointments")
        doctorProfileDbRef = dataBase.getReference("Doctors")
        userProfileDbRef = dataBase.getReference("Users")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        doctorUid = auth.currentUser?.uid!!
        userId = model.uId

        reloadTimeSlot(doctorUid, doctorProfileDbRef, timeFormat, binding.slotDate.text.toString())

        binding.slotDateLayout.setEndIconOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val formattedDate = dateFormat.format(selectedDate.time)
                    binding.slotDate.setText(formattedDate.toString())
                    reloadTimeSlot(
                        doctorUid, doctorProfileDbRef, timeFormat, binding.slotDate.text.toString()
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }


        binding.scheduleAppointment.setOnClickListener(View.OnClickListener {
            slotDate = binding.slotDate.text.toString()
            val selectedDate = dateFormat.parse(slotDate)
            val currentDate = dateFormat.parse(todayDate)
            if (selectedDate!!.before(currentDate)) {
                Toast.makeText(
                    this@DoctorChangeAppointmentActivity,
                    getString(R.string.select_another_date),
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } else {
                if (slotSelected) {
                    if (userId != null) {

                        cancelAppointment()
                    }
                } else {
                    Toast.makeText(
                        this@DoctorChangeAppointmentActivity,
                        getString(R.string.select_time_slot),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnClickListener
                }
            }
        })
    }

    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("model") as DoctorAppointmentListModel
    }

    private fun reloadTimeSlot(
        doctorUid: String,
        doctorProfileDbRef: DatabaseReference,
        timeFormat: SimpleDateFormat,
        slotDate: String
    ) {
        val slotList = ArrayList<TimeSlotModel>()
        val slotAdapter = TimeSlotAdapter(
            this@DoctorChangeAppointmentActivity,
            slotList,
            slotDate,
            this@DoctorChangeAppointmentActivity
        )
        binding.recyclerSlot.adapter = slotAdapter
        doctorProfileDbRef.child(doctorUid).child("Profile")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    slotList.clear()
                    val startTime = snapshot.child("startTime").value.toString()
                    val endTime = snapshot.child("endTime").value.toString()
                    val date1: Date = timeFormat.parse(startTime)
                    val date2: Date = timeFormat.parse(endTime)
                    val timeInMillis: Long = date2.time - date1.time
                    val diffMinutes = ((timeInMillis / 1000) / 60) / 30
                    var st = date1.time
                    for (i in 1..diffMinutes) {
                        doctorProfileDbRef.child(doctorUid).child("appointments").child(slotDate)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val snapList = ArrayList<String>()
                                        snapList.clear()
                                        for (ds in snapshot.children) {
                                            snapList.add(ds.child("time").value.toString().toLowerCase())
                                            Log.d("TAGCHILD", "onDataChange: ${ds.child("time").value.toString()}")
                                        }
                                        val et = st + 1800000
                                        Log.d("TAGCHILD", "onDataChange: ${getTime(st)} - ${getTime(et)}")
                                        if (snapList.contains("${getTime(st)} - ${getTime(et)}".toLowerCase())) {
                                            slotList.add(
                                                TimeSlotModel(
                                                    "${getTime(st)} - ${getTime(et)}",
                                                    "taken",
                                                    false
                                                )
                                            )
                                        } else {
                                            slotList.add(
                                                TimeSlotModel(
                                                    "${getTime(st)} - ${getTime(et)}",
                                                    "available",
                                                    false
                                                )
                                            )
                                        }
                                        st = et
                                        slotAdapter.updateList(slotList)
                                    } else {
                                        val et = st + 1800000
                                        slotList.add(
                                            TimeSlotModel(
                                                "${getTime(st)} - ${getTime(et)}",
                                                "available",
                                                false
                                            )
                                        )
                                        st = et
                                        slotAdapter.updateList(slotList)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getTime(millis: Long): String {
        val formatter = SimpleDateFormat("hh:mm a")
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(millis)
        return formatter.format(calendar.time)
    }

    override fun OnTimeSlotSelected(slot: String, selected: Boolean) {
        this.slotSelected = selected
        if (selected) {
            timeSlot = slot
        } else {
            timeSlot = ""
        }
    }

    private fun cancelAppointment() {
        appointDbRef.child(userId).child(doctorUid).child(model.key).removeValue()
            .addOnSuccessListener {
                cancelAppointmentDoctor()
            }.addOnFailureListener {
                Toast.makeText(this@DoctorChangeAppointmentActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun cancelAppointmentDoctor() {
        appointDbRef.child(doctorUid).child(userId).child(model.key).removeValue()
            .addOnSuccessListener {
                deleteDoctorAppointment()
            }.addOnFailureListener {
                Toast.makeText(this@DoctorChangeAppointmentActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteDoctorAppointment() {
        doctorProfileDbRef.child(doctorUid).child("appointments").child(model.date).child(model.key)
            .removeValue().addOnSuccessListener {
                val userDbRef = appointDbRef.child(userId).child(doctorUid)
                val doctorDbRef = appointDbRef.child(doctorUid).child(userId)
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
                                        doctorProfileDbRef.child(doctorUid).child("appointments")
                                            .child(slotDate).child("$pushId").setValue(slotMap)
                                            .addOnCompleteListener(OnCompleteListener { slotAppoint ->
                                                if (slotAppoint.isSuccessful) {
                                                    getUserFCMToken(userId)
                                                    Toast.makeText(
                                                        this@DoctorChangeAppointmentActivity,
                                                        getString(R.string.successful_rescheduled),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish()

                                                }
                                            })
                                    }
                                })
                        }
                    })
            }.addOnFailureListener {
                Toast.makeText(this@DoctorChangeAppointmentActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getUserFCMToken(userId: String) {
        userProfileDbRef.child(userId).child(AppConstants.PROFILE_REF).child("fcmToken").get()
            .addOnCompleteListener { task -> sendNotification(task.result.value.toString()) }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@DoctorChangeAppointmentActivity, e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotification(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", appSharedPreferences.getString("doctorName"))
                    put("body", "Rescheduled an appointment.")
                    put("userType", "doctorChangeAppointment")
                    put("uid", appSharedPreferences.getString("doctorUid"))
                    put("name", appSharedPreferences.getString("doctorName"))
                    put("imgUrl", appSharedPreferences.getString("doctorImgUrl"))
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
                this@DoctorChangeAppointmentActivity.runOnUiThread {
                    Toast.makeText(
                        this@DoctorChangeAppointmentActivity, e.message, Toast.LENGTH_SHORT
                    ).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

            }
        })
    }
}