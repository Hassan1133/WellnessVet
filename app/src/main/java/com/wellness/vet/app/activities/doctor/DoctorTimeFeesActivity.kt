package com.wellness.vet.app.activities.doctor

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityDoctorTimeFeesBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorProfileTimeModel
import com.wellness.vet.app.models.UserProfileModel
import java.text.SimpleDateFormat
import java.util.Calendar

class DoctorTimeFeesActivity : AppCompatActivity(),OnClickListener {

    private lateinit var binding:ActivityDoctorTimeFeesBinding
    private lateinit var doctorProfileDatabaseRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorTimeFeesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        binding.startTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.startTime.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }


        binding.endTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.endTime.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.saveProfile.setOnClickListener(this)
        appSharedPreferences = AppSharedPreferences(this@DoctorTimeFeesActivity)
        doctorProfileDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.DOCTOR_REF).child(
                appSharedPreferences.getString("doctorUid")!!
            ).child(AppConstants.PROFILE_REF)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveProfile -> {
                if (isValid()) {
                    setDataToModel(
                        binding.startTime.text.toString(),
                        binding.endTime.text.toString(),
                        binding.fees.text.toString()
                    )
                    loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorTimeFeesActivity)
                }
            }
        }
    }

    private fun setDataToModel(startTime: String, endTime: String, fees: String) {
        val doctorProfileTimeModel = DoctorProfileTimeModel()
        doctorProfileTimeModel.startTime = startTime
        doctorProfileTimeModel.endTime = endTime
        doctorProfileTimeModel.fees = fees

        saveDataToDb(doctorProfileTimeModel)
    }

    private fun saveDataToDb(doctorProfileTimeModel: DoctorProfileTimeModel) {
        doctorProfileDatabaseRef.setValue(doctorProfileTimeModel).addOnSuccessListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorTimeFeesActivity, "Profile Data Added Successfully", Toast.LENGTH_SHORT
            ).show()
            appSharedPreferences.put("doctorTimeAdded", true)
            startActivity(Intent(this@DoctorTimeFeesActivity, DoctorDashBoardActivity::class.java))
            finish()
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorTimeFeesActivity, it.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.startTime.text.isNullOrEmpty()) {
            binding.startTime.error = "Please enter start time"
            valid = false
        }

        if (binding.endTime.text.isNullOrEmpty()) {
            binding.endTime.error = "Please enter valid end time"
            valid = false
        }

        if (binding.fees.text.isNullOrEmpty()) {
            binding.fees.error = "Please enter valid fees"
            valid = false
        }

        return valid
    }
}