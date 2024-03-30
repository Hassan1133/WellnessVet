package com.wellness.vet.app.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.activities.user.UserLoginSignupActivity
import com.wellness.vet.app.activities.user.UserProfileActivity
import com.wellness.vet.app.main_utils.AppSharedPreferences

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@SplashActivity)

        Handler(Looper.getMainLooper()).postDelayed({
            val userLoginCheck = appSharedPreferences.getBoolean("userLogin")
            val userProfileCheck = appSharedPreferences.getBoolean("userProfileAdded")
            val docLoginCheck = appSharedPreferences.getBoolean("doctorLogin")

            val intent: Intent = if (userLoginCheck && userProfileCheck) {
                Intent(this, UserDashBoardActivity::class.java)
            } else if (userLoginCheck) {
                Intent(this, UserProfileActivity::class.java)
            } else {
                Intent(this, UserLoginSignupActivity::class.java)
            }

            startActivity(intent)
            finish()

        }, 3000)
    }
}