package com.wellness.vet.app.activities.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.activities.user.UserLoginSignupActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({

            val pref = getSharedPreferences("wellnessVetLogin", Context.MODE_PRIVATE)
            val userCheck = pref.getBoolean("userFlag", false)
            val docCheck = pref.getBoolean("doctorFlag", false)

            val intent: Intent = if (userCheck) {
                Intent(this, UserDashBoardActivity::class.java)
            } else if (docCheck) {
                Intent(this, DoctorDashBoardActivity::class.java)
            } else {
                Intent(this, UserLoginSignupActivity::class.java)
            }

            startActivity(intent)
            finish()

        }, 3000)
    }
}