package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityCreateAppointmentBinding

class CreateAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAppointmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataBase = Firebase.database
        val appointDbRef = dataBase.getReference("appointments")
        val doctorProfileDbRef = dataBase.getReference("Doctors")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val doctorUid = intent.getStringExtra("uid").toString()
            val userUid = currentUser.uid

            val userDbRef = appointDbRef.child(userUid).child(doctorUid)
            val doctorDbRef = appointDbRef.child(doctorUid).child(userUid)

            val pushIdRef = userDbRef.push()
            val pushId = pushIdRef.key

            val mMap = HashMap<String, Any>()
            mMap["date"] = "15-04-2024"
            mMap["time"] = "09:00 am"

            userDbRef.child("$pushId").setValue(mMap)
                .addOnCompleteListener(OnCompleteListener { uAppoint ->
                    if (uAppoint.isSuccessful) {
                        doctorDbRef.child("$pushId").setValue(mMap)
                            .addOnCompleteListener(OnCompleteListener { dAppoint ->
                                if (dAppoint.isSuccessful) {
                                    val slotMap = HashMap<String, Any>()
                                    slotMap["time"] = "09:00 am"
                                    doctorProfileDbRef.child(doctorUid).child("appointments")
                                        .child("15-04-2024").child("$pushId").setValue(slotMap) .addOnCompleteListener(OnCompleteListener { slotAppoint ->
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

    }
}