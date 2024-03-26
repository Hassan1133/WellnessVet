package com.wellness.vet.app.activities.doctor

import android.os.Bundle
<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity
=======
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
>>>>>>> a9967a055d249a59a54af5456743e6730ef9a0ed
import com.wellness.vet.app.R

class DoctorLoginSignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
        setContentView(R.layout.activity_doctor_login_signup)
=======
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_login_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
>>>>>>> a9967a055d249a59a54af5456743e6730ef9a0ed
    }
}