package com.wellness.vet.app.activities.doctor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserLoginSignupActivity
import com.wellness.vet.app.databinding.ActivityDoctorLoginSignupBinding

class DoctorLoginSignupActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding : ActivityDoctorLoginSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init()
    {
        binding.loginAsUserBtn.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.loginAsUserBtn -> {
                startActivity(
                    Intent(
                        this@DoctorLoginSignupActivity,
                        UserLoginSignupActivity::class.java
                    )
                )
                finish()
            }
        }
    }
}