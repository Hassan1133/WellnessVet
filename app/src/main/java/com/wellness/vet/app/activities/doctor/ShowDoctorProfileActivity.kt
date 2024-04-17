package com.wellness.vet.app.activities.doctor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wellness.vet.app.databinding.ActivityShowDoctorProfileBinding
import com.wellness.vet.app.main_utils.AppSharedPreferences

class ShowDoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowDoctorProfileBinding
    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
        getProfileDataFromSharedPref()
    }

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@ShowDoctorProfileActivity)
        getProfileDataFromSharedPref()

        binding.editProfileBtn.setOnClickListener{
            startActivity(Intent(this@ShowDoctorProfileActivity, EditDoctorProfileActivity::class.java))
        }
    }

    private fun getProfileDataFromSharedPref() {
        binding.name.text = appSharedPreferences.getString("doctorName")
        binding.phoneNumber.text = appSharedPreferences.getString("doctorPhoneNo")
        binding.city.text = appSharedPreferences.getString("doctorCity")
        binding.gender.text = appSharedPreferences.getString("doctorGender")
        binding.clinicLocation.text = appSharedPreferences.getString("doctorClinicLocation")
        binding.startTime.text = appSharedPreferences.getString("doctorStartTime")
        binding.endTime.text = appSharedPreferences.getString("doctorEndTime")
        binding.fees.text = appSharedPreferences.getString("doctorFees")
        binding.email.text = appSharedPreferences.getString("doctorEmail")
        Glide.with(applicationContext).load(appSharedPreferences.getString("doctorImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profileImage)
    }
}