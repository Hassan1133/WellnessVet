package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityEditUserProfileBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.USER_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUserProfileBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private val cityList = mutableListOf<String>()
    private lateinit var userProfileRef: DatabaseReference
    private var imageUri = Uri.EMPTY
    private lateinit var loadingDialog: Dialog
    private lateinit var storageRef: StorageReference
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
        storageRef = FirebaseStorage.getInstance().getReference()
        getDataFromSharedPreferences()
        getCityNamesFromDb()

        binding.updateProfileBtn.setOnClickListener {
            if (isValid()) {

                if (imageUri == Uri.EMPTY) {
                    loadingDialog = LoadingDialog.showLoadingDialog(this@EditUserProfileActivity)
                    updateUserProfileData(
                        binding.name.text.toString(),
                        binding.city.text.toString(),
                        appSharedPreferences.getString("userImgUrl")!!
                    )

                } else if (imageUri != Uri.EMPTY) {
                    loadingDialog = LoadingDialog.showLoadingDialog(this@EditUserProfileActivity)
                    uploadImage(
                        binding.name.text.toString(),
                        binding.city.text.toString(), appSharedPreferences.getString("userUid")!!
                    )
                }
            }

        }

        binding.pickImgBtn.setOnClickListener {
            pickNewImage()
        }
    }

    private fun pickNewImage() {
        getContent.launch("image/*")
    }

    private var getContent =
        registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) { result ->
            if (result != null) {
                imageUri = result
                binding.userImg.setImageURI(imageUri)
            }
        }


    private fun updateUserProfileData(name: String, city: String, imgUrl: String) {
        val userMap = hashMapOf<String, Any>(
            "name" to name,
            "city" to city,
            "imgUrl" to imgUrl
        )
        userProfileRef.updateChildren(userMap).addOnSuccessListener {
            updateSharedPreferences(name, city, imgUrl)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@EditUserProfileActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSharedPreferences(name: String, city: String, imgUrl: String) {
        appSharedPreferences.put("userName", name)
        appSharedPreferences.put("userCity", city)
        appSharedPreferences.put("userImgUrl", imgUrl)
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(this@EditUserProfileActivity, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show()
    }

    private fun getDataFromSharedPreferences() {
        binding.name.setText(appSharedPreferences.getString("userName"))
        binding.city.setText(appSharedPreferences.getString("userCity"))
        Glide.with(applicationContext).load(appSharedPreferences.getString("userImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.userImg)
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

    private fun uploadImage(
        name: String,
        city: String,
        userId: String
    ) {

        val filepath: StorageReference = storageRef.child("UserProfileImages/" + userId)

        val uploadTask = filepath.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL
            val downloadUrlTask = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnSuccessListener { uri ->
                updateUserProfileData(
                    name,
                    city,
                    uri.toString()
                )
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@EditUserProfileActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@EditUserProfileActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

}