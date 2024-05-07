package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otpview.OTPListener
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
import java.util.concurrent.TimeUnit

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
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var appSharedPreferences: AppSharedPreferences

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

        appSharedPreferences = AppSharedPreferences(this@UserSignupActivity)

        storageRef = FirebaseStorage.getInstance().getReference()

        usersRef = FirebaseDatabase.getInstance().getReference(USER_REF)

        firebaseAuth = FirebaseAuth.getInstance()

        getCityNamesFromDb()

        startOtpVerificationCode()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pickImgBtn -> {
                chooseImage()
            }

            R.id.signUpBtn -> {
                if (isValid()) {
                    signup()
                    binding.signUpBtn.isEnabled = false
                }
            }

            R.id.signInTxt -> {
                startActivity(Intent(this@UserSignupActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
    private fun signup() {

        loadingDialog = LoadingDialog.showLoadingDialog(this@UserSignupActivity)
        // Retrieve phone number from input field
        var number = binding.phoneNumber.text?.trim().toString()
        // Add country code to phone number
        number = "+92$number"
        // Send verification code to provided phone number
        sendVerificationCode(number)
    }
    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG", "Auth started")
    }

    private fun startOtpVerificationCode() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Called when verification is completed successfully
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Called when verification fails
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Log.d("GFG", "onVerificationFailed  $e")
            }

            override fun onCodeSent(
                verificationId: String, token: PhoneAuthProvider.ForceResendingToken
            ) {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                // Called when verification code is successfully sent
                Log.d("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Request focus on OTP input field and set OTP listener
                binding.otpView.requestFocusOTP()
                binding.otpView.otpListener = object : OTPListener {
                    override fun onInteractionListener() {}

                    override fun onOTPComplete(otp: String) {
                        // Called when OTP is complete
                        val credential: PhoneAuthCredential =
                            PhoneAuthProvider.getCredential(storedVerificationId, otp)
                        signInWithPhoneAuthCredential(credential)
                    }
                }
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                checkUserExistOrNot(
                    task.result.user?.uid.toString(), task.result.user?.phoneNumber.toString()
                )
            } else {
                // If sign-in fails, display error message
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    binding.signUpBtn.isEnabled = true
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkUserExistOrNot(userId: String, phoneNo: String) {
        usersRef.child(userId).child(AppConstants.PROFILE_REF)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Handle data retrieval success
                    if (dataSnapshot.exists()) {
                        Toast.makeText(
                            this@UserSignupActivity,
                            getString(R.string.users_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.signUpBtn.isEnabled = true
                    } else {
                        // Data doesn't exist
                        addDataToModel(userId, phoneNo)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@UserSignupActivity, databaseError.message, Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
            Toast.makeText(this, getString(R.string.select_an_image), Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = getString(R.string.enter_valid_name)
            valid = false
        }
        if (binding.phoneNumber.text!!.length < 11) {
            binding.phoneNumber.error = getString(R.string.enter_valid_phone_number)
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = getString(R.string.enter_valid_city)
            valid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
            binding.email.error = getString(R.string.enter_valid_email)
            valid = false
        }

        return valid
    }


    private fun addDataToModel(userId: String, phoneNo: String)
    {
        val radio: RadioButton = findViewById(binding.genderRadioGroup.checkedRadioButtonId)

        val model = UserProfileModel()
        model.id = userId
        model.name = binding.name.getText().toString().trim()
        model.phoneNo = phoneNo
        model.email = binding.email.getText().toString().trim()
        model.city = binding.city.getText().toString().trim()
        model.gender = radio.text.toString()

        uploadImage(model)
    }

    private fun uploadImage(model: UserProfileModel) {

        loadingDialog = LoadingDialog.showLoadingDialog(this@UserSignupActivity)

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
                getFCMToken(model)
            }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@UserSignupActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFCMToken(userProfileModel: UserProfileModel) {

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Update FCM token in Firestore
            setFCMTokenToDb(token, userProfileModel)
        }.addOnFailureListener { exception ->
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@UserSignupActivity, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFCMTokenToDb(token: String, userProfileModel: UserProfileModel) {

        val map = HashMap<String, Any>()
        map["fcmToken"] = token
        usersRef.child(userProfileModel.id).child(AppConstants.PROFILE_REF).updateChildren(map).addOnSuccessListener {
            userProfileModel.fcmToken = token
            goToUserDashBoard(userProfileModel)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@UserSignupActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToUserDashBoard(userProfileModel: UserProfileModel) {
        appSharedPreferences.put("userPhoneNo", userProfileModel.phoneNo)
        appSharedPreferences.put("userUid", userProfileModel.id)
        appSharedPreferences.put("userName", userProfileModel.name)
        appSharedPreferences.put("userCity", userProfileModel.city)
        appSharedPreferences.put("userGender", userProfileModel.gender)
        appSharedPreferences.put("userImgUrl", userProfileModel.imgUrl)
        appSharedPreferences.put("userEmail", userProfileModel.email)
        appSharedPreferences.put("userLogin", true)
        appSharedPreferences.put("is_lang_set", true)
        appSharedPreferences.put("userType", "user")

        if (!appSharedPreferences.getString("userPhoneNo")
                .isNullOrEmpty() && !appSharedPreferences.getString("userUid")
                .isNullOrEmpty() && !appSharedPreferences.getString("userName")
                .isNullOrEmpty() && !appSharedPreferences.getString("userCity")
                .isNullOrEmpty() && !appSharedPreferences.getString("userGender")
                .isNullOrEmpty() && !appSharedPreferences.getString("userImgUrl")
                .isNullOrEmpty() && !appSharedPreferences.getString("userEmail")
                .isNullOrEmpty() && !appSharedPreferences.getString("userType")
                .isNullOrEmpty() && appSharedPreferences.getBoolean("userLogin") && appSharedPreferences.getBoolean(
                "is_lang_set"
            )
        ) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@UserSignupActivity, getString(R.string.signup_successfully), Toast.LENGTH_SHORT
            ).show()
            startActivity(
                Intent(
                    this@UserSignupActivity, UserDashBoardActivity::class.java
                )
            )
            finish()
        } else {

            appSharedPreferences.put("userPhoneNo", userProfileModel.phoneNo)
            appSharedPreferences.put("userUid", userProfileModel.id)
            appSharedPreferences.put("userName", userProfileModel.name)
            appSharedPreferences.put("userCity", userProfileModel.city)
            appSharedPreferences.put("userGender", userProfileModel.gender)
            appSharedPreferences.put("userImgUrl", userProfileModel.imgUrl)
            appSharedPreferences.put("userEmail", userProfileModel.email)
            appSharedPreferences.put("userLogin", true)
            appSharedPreferences.put("is_lang_set", true)
            appSharedPreferences.put("userType", "user")

            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@UserSignupActivity, getString(R.string.signup_successfully), Toast.LENGTH_SHORT
            ).show()
            startActivity(
                Intent(
                    this@UserSignupActivity, UserDashBoardActivity::class.java
                )
            )
            finish()
        }

    }
}