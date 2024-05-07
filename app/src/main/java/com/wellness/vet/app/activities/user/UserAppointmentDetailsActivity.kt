package com.wellness.vet.app.activities.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityUserAppointmentDetailsBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.models.UserAppointmentListModel
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UserAppointmentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAppointmentDetailsBinding
    private lateinit var model: UserAppointmentListModel
    private var docId = ""
    private var appointKey = ""
    private var appointDate = ""
    private var appointStatus = ""
    private var clinicLatitude: Double = 0.0
    private var clinicLongitude: Double = 0.0
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var appointmentRef: DatabaseReference
    private lateinit var doctorRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getDataFromIntent()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@UserAppointmentDetailsActivity)
        appointmentRef = FirebaseDatabase.getInstance().getReference("appointments")
        doctorRef = FirebaseDatabase.getInstance().getReference(AppConstants.DOCTOR_REF)
        appSharedPreferences = AppSharedPreferences(this@UserAppointmentDetailsActivity)
        binding.directionsBtn.setOnClickListener {
            getLastLocation()
        }
        binding.cancelAppointBtn.setOnClickListener {
            if (appointStatus != "cancelled") {
                if (appointKey.isNotEmpty() && docId.isNotEmpty() && appointDate.isNotEmpty()) {
                    cancelAppointment()
                }
            } else {
                Toast.makeText(
                    this@UserAppointmentDetailsActivity,
                    getString(R.string.cancel_already_appointment),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun cancelAppointment() {
        val map = HashMap<String, Any>()
        map["appointmentStatus"] = "cancelled"
        appointmentRef.child(appSharedPreferences.getString("userUid")!!).child(docId)
            .child(appointKey).updateChildren(map).addOnSuccessListener {
                cancelAppointmentDoctor()
            }.addOnFailureListener {
                Toast.makeText(this@UserAppointmentDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun cancelAppointmentDoctor() {
        val map = HashMap<String, Any>()
        map["appointmentStatus"] = "cancelled"
        appointmentRef.child(docId).child(appSharedPreferences.getString("userUid")!!)
            .child(appointKey).updateChildren(map).addOnSuccessListener {
                deleteDoctorAppointment()
            }.addOnFailureListener {
                Toast.makeText(this@UserAppointmentDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteDoctorAppointment() {
        doctorRef.child(docId).child("appointments").child(appointDate).child(appointKey)
            .removeValue().addOnSuccessListener {
                getDoctorFCMToken(docId)
                Toast.makeText(
                    this@UserAppointmentDetailsActivity,
                    getString(R.string.appointment_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@UserAppointmentDetailsActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("userAppointmentListModel") as UserAppointmentListModel
        binding.name.text = model.name
        binding.clinicLocation.text = model.clinicLocation
        binding.timings.text = "${model.startTime} - ${model.endTime}"
        binding.appointTime.text = model.time
        appointDate = model.date
        binding.appointDate.text = appointDate
        docId = model.uId
        appointKey = model.key
        clinicLatitude = model.clinicLatitude.toDouble()
        clinicLongitude = model.clinicLongitude.toDouble()
        appointStatus = model.appointmentStatus
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@UserAppointmentDetailsActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@UserAppointmentDetailsActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            userLatitude = it.latitude
            userLongitude = it.longitude

            if (userLatitude != 0.0 && userLongitude != 0.0 && clinicLatitude != 0.0 && clinicLongitude != 0.0) {
                openGoogleMapsForDirections(
                    userLatitude,
                    userLongitude,
                    clinicLatitude,
                    clinicLongitude
                )
            }
        }
    }

    private fun openGoogleMapsForDirections(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ) {
        //Uri with the destination coordinates
        // http://maps.google.com/maps?saddr=37.7749,-122.4194&daddr=34.0522,-118.2437
        val gmmIntentUri =
            Uri.parse("http://maps.google.com/maps?saddr=$startLat,$startLon&daddr=$destLat,$destLon")

        //Intent with the action to view and set the data to the Uri
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        //package to ensure only the Google Maps app is used
        mapIntent.setPackage("com.google.android.apps.maps")

        // Check if there is an app available to handle the Intent before starting it
        if (mapIntent.resolveActivity(this@UserAppointmentDetailsActivity.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(
                this@UserAppointmentDetailsActivity,
                "Google Maps not installed in this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getDoctorFCMToken(doctorId: String) {
        doctorRef.child(doctorId).child(AppConstants.PROFILE_REF).child("fcmToken").get()
            .addOnCompleteListener { task -> sendNotification(task.result.value.toString()) }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@UserAppointmentDetailsActivity, e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotification(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", appSharedPreferences.getString("userName"))
                    put("body", "Cancelled an appointment.")
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
                this@UserAppointmentDetailsActivity.runOnUiThread {
                    Toast.makeText(
                        this@UserAppointmentDetailsActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

            }
        })
    }
}