package com.wellness.vet.app.activities.user

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.SplashActivity
import com.wellness.vet.app.databinding.ActivityShowUserProfileBinding
import com.wellness.vet.app.main_utils.AppSharedPreferences
import java.util.Locale

class ShowUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowUserProfileBinding
    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
        getDataFromSharedPreferences()
    }

    private fun init()
    {
        appSharedPreferences = AppSharedPreferences(this@ShowUserProfileActivity)
        getDataFromSharedPreferences()

        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this@ShowUserProfileActivity, EditUserProfileActivity::class.java))
        }

        binding.languageBtn.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val builder = AlertDialog.Builder(this@ShowUserProfileActivity)
        builder.setTitle(getString(R.string.choose_lang))
        builder.setSingleChoiceItems(
            resources.getStringArray(R.array.languages),
            -1
        ) { _, which ->
            if (which == 0) {
                setLocale("")
                startActivity(Intent(this@ShowUserProfileActivity, SplashActivity::class.java))
                finish()
            } else if (which == 1) {
                setLocale("ur")
                startActivity(Intent(this@ShowUserProfileActivity, SplashActivity::class.java))
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

    private fun getDataFromSharedPreferences()
    {
        binding.name.text = appSharedPreferences.getString("userName")
        binding.phoneNumber.text = appSharedPreferences.getString("userPhoneNo")
        binding.city.text = appSharedPreferences.getString("userCity")
        binding.gender.text = appSharedPreferences.getString("userGender")
        binding.email.text = appSharedPreferences.getString("userEmail")
        binding.accountNumber.text = appSharedPreferences.getString("userAccountNumber")
        Glide.with(applicationContext).load(appSharedPreferences.getString("userImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profile)
    }
}