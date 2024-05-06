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
import com.wellness.vet.app.databinding.ActivityUserAppointmentDetailsBinding
import com.wellness.vet.app.models.UserAppointmentListModel

class UserAppointmentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAppointmentDetailsBinding
    private lateinit var model: UserAppointmentListModel
    private var docId = ""
    private var appointKey = ""
    private var clinicLatitude: Double = 0.0
    private var clinicLongitude: Double = 0.0
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
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
        binding.directionsBtn.setOnClickListener {
            getLastLocation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("userAppointmentListModel") as UserAppointmentListModel
        binding.name.text = model.name
        binding.clinicLocation.text = model.clinicLocation
        binding.timings.text = "${model.startTime} - ${model.endTime}"
        binding.appointTime.text = model.time
        binding.appointDate.text = model.date
        docId = model.uId
        appointKey = model.key
        clinicLatitude = model.clinicLatitude.toDouble()
        clinicLongitude = model.clinicLongitude.toDouble()
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
}