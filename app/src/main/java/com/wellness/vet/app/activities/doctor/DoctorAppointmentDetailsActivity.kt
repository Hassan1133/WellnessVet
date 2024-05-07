package com.wellness.vet.app.activities.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityDoctorAppointmentDetailsBinding
import com.wellness.vet.app.models.DoctorAppointmentListModel
import com.wellness.vet.app.models.UserAppointmentListModel

class DoctorAppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorAppointmentDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init()
    {
        getDataFromIntent()
    }

    private fun getDataFromIntent() {
        val model = intent.getSerializableExtra("doctorAppointmentListModel") as DoctorAppointmentListModel
        binding.name.text = model.name
        binding.appointTime.text = model.time
        binding.appointDate.text = model.date
        binding.appointStatus.text = model.appointmentStatus
    }
}