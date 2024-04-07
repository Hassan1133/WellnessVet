package com.wellness.vet.app.activities.common

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorChatActivity
import com.wellness.vet.app.activities.user.UserChatActivity

class MainTestAuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test_auth)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
//            startActivity(Intent(this@MainTestAuthActivity, UserChatActivity::class.java))
            startActivity(Intent(this@MainTestAuthActivity,DoctorChatActivity::class.java))
        } else {
//            doctor@abc.com
            auth.signInWithEmailAndPassword("doctor@abc.com", "112233")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        startActivity(Intent(this@MainTestAuthActivity, DoctorChatActivity::class.java))
                    } else {

                    }
                }
        }
    }
}