package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityEditUserProfileBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.USER_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUserProfileBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private val cityList = mutableListOf<String>()
    private lateinit var userProfileRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@EditUserProfileActivity)
        userProfileRef = FirebaseDatabase.getInstance().getReference(USER_REF).child(
            appSharedPreferences.getString("userUid")!!
        ).child(PROFILE_REF)
        getDataFromSharedPreferences()
        getCityNamesFromDb()

        binding.updateProfileBtn.setOnClickListener {
            if (isValid()) {
                updateUserProfileData(binding.name.text.toString(), binding.city.text.toString())
            }
        }
    }

    private fun updateUserProfileData(name: String, city: String) {
        val userMap = hashMapOf<String, Any>(
            "name" to name,
            "city" to city,
        )
        userProfileRef.updateChildren(userMap).addOnSuccessListener {
            updateSharedPreferences(name, city)
        }.addOnFailureListener {
            Toast.makeText(this@EditUserProfileActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSharedPreferences(name: String, city: String) {
        appSharedPreferences.put("userName", name)
        appSharedPreferences.put("userCity", city)
        Toast.makeText(this@EditUserProfileActivity, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show()
    }

    private fun getDataFromSharedPreferences() {
        binding.name.setText(appSharedPreferences.getString("userName"))
        binding.city.setText(appSharedPreferences.getString("userCity"))
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
                            ArrayAdapter(
                                this@EditUserProfileActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@EditUserProfileActivity,
                        error.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            })
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = "Please select city"
            valid = false
        }

        return valid
    }
}