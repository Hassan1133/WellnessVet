package com.wellness.vet.app.activities.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.databinding.ActivityDoctorDashBoardBinding

class DoctorDashBoardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDoctorDashBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}