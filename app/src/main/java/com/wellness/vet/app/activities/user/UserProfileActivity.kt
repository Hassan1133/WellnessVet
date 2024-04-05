package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityUserProfileBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.CITY_NAMES
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.USER_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.UserProfileModel

class UserProfileActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var userProfileDatabaseRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private val cityList = mutableListOf<String>()
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.saveProfile.setOnClickListener(this)
        appSharedPreferences = AppSharedPreferences(this@UserProfileActivity)
        userProfileDatabaseRef =
            FirebaseDatabase.getInstance().getReference(USER_REF).child(
                appSharedPreferences.getString("userUid")!!
            ).child(PROFILE_REF)
        getCityNamesFromDb()
    }

    private fun getCityNamesFromDb() {
        FirebaseDatabase.getInstance().getReference(CITY_NAMES)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cityList.clear()
                    snapshot.children.forEach {
                        cityList.add(it.value.toString())
                    }

                    if (cityList.isNotEmpty()) {
                        binding.city.setAdapter(
                            ArrayAdapter(
                                this@UserProfileActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, error.toString(), Toast.LENGTH_SHORT)
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
                        appSharedPreferences.getString("userPhoneNo")!!,
                        binding.city.text.toString(),
                        radio.text.toString()
                    )
                    loadingDialog = LoadingDialog.showLoadingDialog(this@UserProfileActivity)
                }
            }
        }
    }

    private fun setDataToModel(name: String, phone: String, city: String, gender: String) {
        val userProfile = UserProfileModel()
        userProfile.name = name
        userProfile.phoneNo = phone
        userProfile.city = city
        userProfile.gender = gender

        saveDataToDb(userProfile)
    }

    private fun saveDataToDb(userProfile: UserProfileModel) {
        userProfileDatabaseRef.setValue(userProfile).addOnSuccessListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@UserProfileActivity, "Profile Data Added Successfully", Toast.LENGTH_SHORT
            ).show()
            updateSharedPref(userProfile.name, userProfile.city, userProfile.gender)
            startActivity(Intent(this@UserProfileActivity, UserDashBoardActivity::class.java))
            finish()
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@UserProfileActivity, it.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateSharedPref(name: String, city: String, gender: String) {
        appSharedPreferences.put("userProfileAdded", true)
        appSharedPreferences.put("userName", name)
        appSharedPreferences.put("userCity", city)
        appSharedPreferences.put("userGender", gender)
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = "Please enter valid name"
            valid = false
        }

        return valid
    }
}