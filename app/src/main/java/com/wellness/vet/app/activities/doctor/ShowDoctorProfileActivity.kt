package com.wellness.vet.app.activities.doctor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityDoctorProfileBinding
import com.wellness.vet.app.databinding.ActivityShowDoctorProfileBinding

class ShowDoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowDoctorProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}