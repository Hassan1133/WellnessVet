package com.wellness.vet.app.activities.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.databinding.ActivityShowUserProfileBinding
import com.wellness.vet.app.main_utils.AppSharedPreferences

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
    }

    private fun getDataFromSharedPreferences()
    {
        binding.name.text = appSharedPreferences.getString("userName")
        binding.phoneNumber.text = appSharedPreferences.getString("userPhoneNo")
        binding.city.text = appSharedPreferences.getString("userCity")
        binding.gender.text = appSharedPreferences.getString("userGender")
    }
}