package com.wellness.vet.app.activities.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.databinding.ActivityDoctorLoginSignupBinding

class DoctorLoginSignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDoctorLoginSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}