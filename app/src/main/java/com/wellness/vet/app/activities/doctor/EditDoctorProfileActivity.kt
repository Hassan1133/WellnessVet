package com.wellness.vet.app.activities.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityEditDoctorProfileBinding

class EditDoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDoctorProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}