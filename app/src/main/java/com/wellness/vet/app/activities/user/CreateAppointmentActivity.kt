package com.wellness.vet.app.activities.user

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
import com.wellness.vet.app.adapters.TimeSlotAdapter
import com.wellness.vet.app.databinding.ActivityCreateAppointmentBinding
import com.wellness.vet.app.interfaces.TimeSlotSelectListener
import com.wellness.vet.app.models.TimeSlotModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateAppointmentActivity : AppCompatActivity(), TimeSlotSelectListener {

    private lateinit var binding: ActivityCreateAppointmentBinding
    private val calendar = Calendar.getInstance()
    private var slotSelected = false
    private lateinit var timeSlot: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataBase = Firebase.database
        val appointDbRef = dataBase.getReference("appointments")
        val doctorProfileDbRef = dataBase.getReference("Doctors")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val doctorUid = intent.getStringExtra("uid").toString()


        val currentTime = Calendar.getInstance().time
        val todayDate = dateFormat.format(currentTime)
        binding.slotDate.setText(todayDate.toString())

        reloadTimeSlot(doctorUid, doctorProfileDbRef, timeFormat, binding.slotDate.text.toString())

        binding.slotDateLayout.setEndIconOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val formattedDate = dateFormat.format(selectedDate.time)
                    binding.slotDate.setText(formattedDate.toString())
                    reloadTimeSlot(
                        doctorUid,
                        doctorProfileDbRef,
                        timeFormat,
                        binding.slotDate.text.toString()
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }


        binding.scheduleAppointment.setOnClickListener(View.OnClickListener {
            val slotDate = binding.slotDate.text.toString()
            val selectedDate = dateFormat.parse(slotDate)
            val currentDate = dateFormat.parse(todayDate)
            if (selectedDate!!.before(currentDate)) {
                Toast.makeText(
                    this@CreateAppointmentActivity,
                    "Please Select Today or any after today Date",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            } else {
                if (slotSelected) {
                    if (currentUser != null) {
                        val userUid = currentUser.uid

                        val userDbRef = appointDbRef.child(userUid).child(doctorUid)
                        val doctorDbRef = appointDbRef.child(doctorUid).child(userUid)

                        val pushIdRef = userDbRef.push()
                        val pushId = pushIdRef.key

                        val mMap = HashMap<String, Any>()
                        mMap["date"] = slotDate
                        mMap["time"] = timeSlot

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
                                                    .child(slotDate).child("$pushId")
                                                    .setValue(slotMap)
                                                    .addOnCompleteListener(OnCompleteListener { slotAppoint ->
                                                        if (slotAppoint.isSuccessful) {
                                                            Toast.makeText(
                                                                this@CreateAppointmentActivity,
                                                                "Success",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    })

                                            }
                                        })
                                }
                            })

                    }
                } else {
                    Toast.makeText(
                        this@CreateAppointmentActivity,
                        "Please Select a Time Slot",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnClickListener
                }
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


    private fun reloadTimeSlot(
        doctorUid: String,
        doctorProfileDbRef: DatabaseReference,
        timeFormat: SimpleDateFormat,
        slotDate: String
    ) {
        val slotList = ArrayList<TimeSlotModel>()
        val slotAdapter = TimeSlotAdapter(
            this@CreateAppointmentActivity,
            slotList,
            this@CreateAppointmentActivity
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
                        doctorProfileDbRef.child(doctorUid).child("appointments")
                            .child(slotDate)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val snapList = ArrayList<String>()
                                        snapList.clear()
                                        for (ds in snapshot.children) {
                                            snapList.add(ds.child("time").value.toString())
                                        }
                                        val et = st + 1800000
                                        if (snapList.contains("${getTime(st)} - ${getTime(et)}")) {
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
}