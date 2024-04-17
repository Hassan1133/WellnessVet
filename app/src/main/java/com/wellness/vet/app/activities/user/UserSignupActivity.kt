package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.LoginActivity
import com.wellness.vet.app.databinding.ActivityUserSignupBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.USER_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.NetworkManager
import com.wellness.vet.app.main_utils.StoragePermissionUtils
import com.wellness.vet.app.models.UserProfileModel

class UserSignupActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityUserSignupBinding
    private lateinit var storagePermissionUtils: StoragePermissionUtils
    private var imageUri: Uri = Uri.EMPTY
    private lateinit var storageRef: StorageReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var loadingDialog: Dialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var networkManager: NetworkManager
    private val cityList = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {

        binding.pickImgBtn.setOnClickListener(this)
        binding.signUpBtn.setOnClickListener(this)
        binding.signInTxt.setOnClickListener(this)

        storagePermissionUtils = StoragePermissionUtils(this)
        // Call the method to check storage permissions
        storagePermissionUtils.checkStoragePermission()

        networkManager = NetworkManager(this@UserSignupActivity)

        storageRef = FirebaseStorage.getInstance().getReference()

        usersRef = FirebaseDatabase.getInstance().getReference(USER_REF)

        firebaseAuth = FirebaseAuth.getInstance()

        getCityNamesFromDb()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pickImgBtn -> {
                chooseImage()
            }

            R.id.signUpBtn -> {
                if (isValid()) {
                    checkUserPhoneExists(binding.phoneNumber.text.toString())
                }
            }

            R.id.signInTxt -> {
                startActivity(Intent(this@UserSignupActivity, LoginActivity::class.java))
                finish()
            }
        }
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
                                this@UserSignupActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserSignupActivity, error.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }


    private fun chooseImage() // method for get image from gallery
    {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            binding.userImg.setImageURI(imageUri)
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (imageUri == Uri.EMPTY) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }
        if (binding.phoneNumber.text!!.length < 11) {
            binding.phoneNumber.error = "Please enter valid phone number"
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = "Please enter valid city"
            valid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
            binding.email.error = "Please enter valid email"
            valid = false
        }

        if (binding.password.text.isNullOrEmpty() || binding.password.text!!.length < 6) {
            binding.password.error = "Please enter valid password"
            valid = false
        }

        return valid
    }

    private fun checkUserPhoneExists(phoneNo: String) {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            var count = 0
            var check = false
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    // data exists in database
                    for (userSnapshot in snapshot.getChildren()) {
                        val profile: UserProfileModel =
                            userSnapshot.child(AppConstants.PROFILE_REF)
                                .getValue(UserProfileModel::class.java)!!
                        count++
                        if (profile.phoneNo == phoneNo) {
                            binding.phoneNumber.setText("")
                            Toast.makeText(
                                this@UserSignupActivity,
                                "Phone number already exists. Please choose a different one",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.phoneNumber.error =
                                "Phone number already exists. Please choose a different one"
                            check = true
                            return
                        } else if (count.toLong() == snapshot.childrenCount) {
                            if (!check) {
                                createAccount()
                            }
                        }
                    }
                } else {
                    createAccount()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserSignupActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createAccount() // method for create account
    {
        loadingDialog = LoadingDialog.showLoadingDialog(this@UserSignupActivity)
        val radio: RadioButton = findViewById(binding.genderRadioGroup.checkedRadioButtonId)

        val model = UserProfileModel()
        model.name = binding.name.getText().toString().trim()
        model.phoneNo = binding.phoneNumber.getText().toString().trim()
        model.email = binding.email.getText().toString().trim()
        model.city = binding.city.getText().toString().trim()
        model.gender = radio.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(
            model.email, binding.password.getText().toString().trim()
        ).addOnCompleteListener(OnCompleteListener<AuthResult?> { task ->
            if (task.isSuccessful) {
                model.id = firebaseAuth.uid.toString()
                uploadImage(model)
            }
        }).addOnFailureListener(OnFailureListener { e ->
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@UserSignupActivity, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun uploadImage(model: UserProfileModel) {


        val filepath: StorageReference = storageRef.child("UserProfileImages/" + model.id)

        val uploadTask = filepath.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL
            val downloadUrlTask = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnSuccessListener { uri ->
                model.imgUrl = uri.toString()
                addDataToDb(model)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@UserSignupActivity, it.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@UserSignupActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDataToDb(model: UserProfileModel) {
        usersRef.child(model.id).child(AppConstants.PROFILE_REF).setValue(model)
            .addOnSuccessListener {
                goToLoginActivity()
            }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@UserSignupActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToLoginActivity() {
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(this@UserSignupActivity, "Signed up successfully", Toast.LENGTH_SHORT)
            .show()
        startActivity(Intent(this@UserSignupActivity, LoginActivity::class.java))
        finish()
    }
}