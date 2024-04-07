package com.wellness.vet.app.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorChatActivity
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.doctor.DoctorProfileActivity
import com.wellness.vet.app.activities.doctor.DoctorTimeFeesActivity
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.activities.user.UserLoginSignupActivity
import com.wellness.vet.app.activities.user.UserProfileActivity
import com.wellness.vet.app.main_utils.AppSharedPreferences

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var intent: Intent
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
            val docProfileCheck = appSharedPreferences.getBoolean("doctorProfileAdded")
            val docTimeCheck = appSharedPreferences.getBoolean("doctorTimeAdded")

            if (userLoginCheck) {
                intent = if (userProfileCheck) {
                    Intent(this, UserDashBoardActivity::class.java)
                } else {
                    Intent(this, UserProfileActivity::class.java)
                }
            } else if (docLoginCheck) {
                intent = if (docProfileCheck && docTimeCheck) {
//                    Intent(this, DoctorDashBoardActivity::class.java)
                    Intent(this, DoctorChatActivity::class.java)
                } else if (docProfileCheck) {
                    Intent(this, DoctorTimeFeesActivity::class.java)
                } else {
                    Intent(this, DoctorProfileActivity::class.java)
                }
            } else {
                intent = Intent(this, UserLoginSignupActivity::class.java)
            }

            startActivity(intent)
            finish()

        }, 3000)
    }
}