package com.wellness.vet.app.activities.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityDoctorAppointmentDetailsBinding
import com.wellness.vet.app.models.DoctorAppointmentListModel

class DoctorAppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorAppointmentDetailsBinding
    private lateinit var model: DoctorAppointmentListModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init()
    {
        getDataFromIntent()

        binding.cancelAppointBtn.setOnClickListener {

            if (model != null) {

                if (model.appointmentStatus != "cancelled") {
                    MaterialAlertDialogBuilder(
                        this@DoctorAppointmentDetailsActivity,
                        R.style.ThemeOverlay_App_MaterialAlertDialog
                    ).setMessage(R.string.cancel_appointment_message).setCancelable(false)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            goToDoctorChangeAppointmentActivity() // Perform logout
                        }.setNegativeButton(R.string.no) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                } else {
                    Toast.makeText(this@DoctorAppointmentDetailsActivity, getString(R.string.appointment_cancelled_user), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.userPetBtn.setOnClickListener {
            val intent = Intent(
                this@DoctorAppointmentDetailsActivity, DoctorUserPetsActivity::class.java
            )
            intent.putExtra("model", model)
            startActivity(intent)
        }
    }

    private fun goToDoctorChangeAppointmentActivity() {
        val intent = Intent(
            this@DoctorAppointmentDetailsActivity, DoctorChangeAppointmentActivity::class.java
        )
        intent.putExtra("model", model)
        startActivity(intent)
    }

    private fun getDataFromIntent() {
        model =
            intent.getSerializableExtra("doctorAppointmentListModel") as DoctorAppointmentListModel
        binding.name.text = model.name
        binding.appointTime.text = model.time
        binding.appointDate.text = model.date
        binding.appointStatus.text = model.appointmentStatus
    }
}