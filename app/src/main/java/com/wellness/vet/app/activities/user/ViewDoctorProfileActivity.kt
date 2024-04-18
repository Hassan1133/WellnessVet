package com.wellness.vet.app.activities.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityViewDoctorProfileBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.models.DoctorDetailProfileModel

class ViewDoctorProfileActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityViewDoctorProfileBinding
    private var doctorUId = ""
    private var docClinicLatitude = 0.0
    private var docClinicLongitude = 0.0
    private var userLatitude = 0.0
    private var userLongitude = 0.0
    private lateinit var doctorRef: DatabaseReference
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        doctorRef = FirebaseDatabase.getInstance().getReference(DOCTOR_REF)
        getDataFromIntent()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@ViewDoctorProfileActivity)
        binding.directionCard.setOnClickListener(this)
    }

    private fun getDataFromIntent() {
        doctorUId = intent.getStringExtra("doctorUId")!!
        getDoctorProfileData(doctorUId)
    }

    private fun getDoctorProfileData(id: String) {
        doctorRef.child(id).child(PROFILE_REF).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorProfile = snapshot.getValue(DoctorDetailProfileModel::class.java)
                setDoctorProfileDataToFields(doctorProfile!!)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewDoctorProfileActivity, error.message, Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDoctorProfileDataToFields(doctorProfile: DoctorDetailProfileModel) {
        binding.doctorName.text = doctorProfile.name
        binding.doctorPhoneNo.text = doctorProfile.phoneNo
        binding.city.text = doctorProfile.city
        binding.timings.text = "${doctorProfile.startTime} - ${doctorProfile.endTime}"
        binding.clinicLocation.text = doctorProfile.clinicLocation
        binding.fees.text = doctorProfile.fees
        docClinicLatitude = doctorProfile.clinicLatitude
        docClinicLongitude = doctorProfile.clinicLongitude
        Glide.with(this@ViewDoctorProfileActivity).load(doctorProfile.imgUrl)
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profileImg)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.directionCard -> {
                getLastLocation()
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@ViewDoctorProfileActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@ViewDoctorProfileActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            userLatitude = it.latitude
            userLongitude = it.longitude

            if (userLatitude != 0.0 && userLongitude != 0.0 && docClinicLatitude != 0.0 && docClinicLongitude != 0.0) {
                openGoogleMapsForDirections(
                    userLatitude,
                    userLongitude,
                    docClinicLatitude,
                    docClinicLongitude
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
        if (mapIntent.resolveActivity(this@ViewDoctorProfileActivity.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(
                this@ViewDoctorProfileActivity,
                "Google Maps not installed in this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}