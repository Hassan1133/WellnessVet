package com.wellness.vet.app.activities.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityDoctorProfileBinding
import com.wellness.vet.app.databinding.ActivityUserDashBoardBinding

class UserDashBoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDashBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}