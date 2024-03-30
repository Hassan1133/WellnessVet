package com.wellness.vet.app.activities.doctor

import android.app.Dialog
import android.content.Intent
import android.opengl.ETC1.isValid
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.adapters.CityDropDownAdapter
import com.wellness.vet.app.databinding.ActivityDoctorProfileBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorProfileModel

class DoctorProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityDoctorProfileBinding
    private lateinit var doctorProfileDatabaseRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private val cityList = mutableListOf<String>()
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.saveProfile.setOnClickListener(this)
        appSharedPreferences = AppSharedPreferences(this@DoctorProfileActivity)
        doctorProfileDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.DOCTOR_REF).child(
                appSharedPreferences.getString("doctorUid")!!
            ).child(AppConstants.PROFILE_REF)
        getCityNamesFromDb()
    }

    private fun getCityNamesFromDb() {
        FirebaseDatabase.getInstance().getReference(AppConstants.CITY_NAMES)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cityList.clear()
                    snapshot.children.forEach {
                        cityList.add(it.value.toString())
                    }

                    if (cityList.isNotEmpty()) {
                        binding.city.setAdapter(
                            CityDropDownAdapter(
                                this@DoctorProfileActivity, cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DoctorProfileActivity, error.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveProfile -> {
                if (isValid()) {
                    val radio: RadioButton =
                        findViewById(binding.genderRadioGroup.checkedRadioButtonId)
                    setDataToModel(
                        binding.name.text.toString(),
                        appSharedPreferences.getString("doctorPhoneNo")!!,
                        binding.city.text.toString(),
                        radio.text.toString()
                    )
                    loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorProfileActivity)
                }
            }
        }
    }

    private fun setDataToModel(name: String, phone: String, city: String, gender: String) {
        val doctorProfile = DoctorProfileModel()
        doctorProfile.name = name
        doctorProfile.phoneNo = phone
        doctorProfile.city = city
        doctorProfile.gender = gender

        saveDataToDb(doctorProfile)
    }

    private fun saveDataToDb(doctorProfile: DoctorProfileModel) {
        doctorProfileDatabaseRef.setValue(doctorProfile).addOnSuccessListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorProfileActivity, "Profile Data Added Successfully", Toast.LENGTH_SHORT
            ).show()
            appSharedPreferences.put("doctorProfileAdded", true)
            startActivity(Intent(this@DoctorProfileActivity, DoctorTimeFeesActivity::class.java))
            finish()
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorProfileActivity, it.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = "Please enter valid city"
            valid = false
        }

        return valid
    }
}