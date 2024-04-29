package com.wellness.vet.app.activities.doctor

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.SplashActivity
import com.wellness.vet.app.databinding.ActivityShowDoctorProfileBinding
import com.wellness.vet.app.main_utils.AppSharedPreferences
import java.util.Locale

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

        binding.languageBtn.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val builder = AlertDialog.Builder(this@ShowDoctorProfileActivity)
        builder.setTitle(getString(R.string.choose_lang))
        builder.setSingleChoiceItems(
            resources.getStringArray(R.array.languages),
            -1
        ) { _, which ->
            if (which == 0) {
                setLocale("")
                startActivity(Intent(this@ShowDoctorProfileActivity, SplashActivity::class.java))
                finish()
            } else if (which == 1) {
                setLocale("ur")
                startActivity(Intent(this@ShowDoctorProfileActivity, SplashActivity::class.java))
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(
            configuration,
            baseContext.resources.displayMetrics
        )

        appSharedPreferences.put("app_lang", language)
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
        binding.accountNumber.text = appSharedPreferences.getString("doctorAccountNumber")
        Glide.with(applicationContext).load(appSharedPreferences.getString("doctorImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profileImage)
    }
}