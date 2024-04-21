package com.wellness.vet.app.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.user.UserDashBoardActivity
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

            if (FirebaseAuth.getInstance().currentUser != null) {

                val userLoginCheck = appSharedPreferences.getBoolean("userLogin")
                val docLoginCheck = appSharedPreferences.getBoolean("doctorLogin")

                 intent = when {
                    userLoginCheck -> Intent(this, UserDashBoardActivity::class.java)
                    docLoginCheck -> Intent(this, DoctorDashBoardActivity::class.java)
                    else -> Intent(
                        this, LoginActivity::class.java
                    ) // Default to LoginActivity if no flags are set
                }

                startActivity(intent)
                finish()
            } else {

                startActivity(Intent(
                    this, LoginActivity::class.java
                ))
                finish()
            }

        }, 3000)
    }
}