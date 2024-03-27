package com.wellness.vet.app.activities.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityUserLoginSignupBinding

class UserLoginSignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUserLoginSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun init()
    {

    }

    override fun onResume() {
        super.onResume()
        init()
    }
}