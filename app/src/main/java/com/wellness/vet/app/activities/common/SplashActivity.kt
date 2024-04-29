package com.wellness.vet.app.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.main_utils.AppSharedPreferences
import java.util.Locale

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

            if (appSharedPreferences.getBoolean("is_lang_set")) {
                setLanguage()
            } else {
                startActivity(Intent(
                    this, SelectLanguageActivity::class.java
                ))
                finish()
            }

        }, 3000)
    }

    private fun setLanguage() {
        val locale = Locale(appSharedPreferences.getString("app_lang")!!)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(
            configuration, baseContext.resources.displayMetrics
        )

        if (FirebaseAuth.getInstance().currentUser != null) {

            val userLoginCheck = appSharedPreferences.getBoolean("userLogin")
            val docLoginCheck = appSharedPreferences.getBoolean("doctorLogin")

            val intent = when {
                userLoginCheck -> Intent(this, UserDashBoardActivity::class.java)
                docLoginCheck -> Intent(this, DoctorDashBoardActivity::class.java)
                else -> Intent(
                    this, LoginActivity::class.java
                ) // Default to LoginActivity if no flags are set
            }

            startActivity(intent)
            finish()
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}