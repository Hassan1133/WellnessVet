package com.wellness.vet.app.activities.doctor

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.wellness.vet.app.activities.common.MapsActivity
import com.wellness.vet.app.databinding.ActivityDoctorSignUpBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.LocationPermissionUtils
import com.wellness.vet.app.main_utils.NetworkManager
import com.wellness.vet.app.main_utils.StoragePermissionUtils
import com.wellness.vet.app.models.DoctorDetailProfileModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit


class DoctorSignUpActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityDoctorSignUpBinding
    private lateinit var storagePermissionUtils: StoragePermissionUtils
    private var imageUri: Uri = Uri.EMPTY
    private var selectedClinicLatitude: Double = 0.0
    private var selectedClinicLongitude: Double = 0.0
    private lateinit var storageRef: StorageReference
    private lateinit var doctorReference: DatabaseReference
    private lateinit var loadingDialog: Dialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var networkManager: NetworkManager
    private lateinit var permissionUtils: LocationPermissionUtils
    private val cityList = mutableListOf<String>()
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var appSharedPreferences: AppSharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        binding.pickImgBtn.setOnClickListener(this)
        binding.signUpBtn.setOnClickListener(this)
        binding.signInTxt.setOnClickListener(this)

        storagePermissionUtils = StoragePermissionUtils(this)
        // Call the method to check storage permissions
        storagePermissionUtils.checkStoragePermission()
        appSharedPreferences = AppSharedPreferences(this@DoctorSignUpActivity)
        networkManager = NetworkManager(this@DoctorSignUpActivity)
        permissionUtils = LocationPermissionUtils(this@DoctorSignUpActivity)
        permissionUtils.checkAndRequestPermissions()

        storageRef = FirebaseStorage.getInstance().getReference()

        doctorReference = FirebaseDatabase.getInstance().getReference(DOCTOR_REF)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.startTimeLayout.setEndIconOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.startTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }


        binding.endTimeLayout.setEndIconOnClickListener() {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.endTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.clinicLocationLayout.setEndIconOnClickListener {
            val intent = Intent(this@DoctorSignUpActivity, MapsActivity::class.java)

            if (networkManager.isNetworkAvailable()) {
                if (permissionUtils.isMapsEnabled()) {
                    if (permissionUtils.isLocationPermissionGranted()) {
                        mapsActivityResultLauncher.launch(intent)
                    } else {
                        permissionUtils.getLocationPermission()
                    }
                }
            } else {
                Toast.makeText(
                    this@DoctorSignUpActivity, getString(R.string.please_connect_to_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        getCityNamesFromDb()
        startOtpVerificationCode()
    }

    private val mapsActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result!!.resultCode == RESULT_OK) {
            binding.clinicLocation.setText(result.data!!.getStringExtra("address"))
            selectedClinicLatitude = result.data!!.getDoubleExtra("latitude", 0.0)
            selectedClinicLongitude = result.data!!.getDoubleExtra("longitude", 0.0)
        }
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
                startActivity(Intent(this@DoctorSignUpActivity, LoginActivity::class.java))
                finish()
            }
        }
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
                checkDoctorProfileExistOrNot(
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

    private fun signup() {

        loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorSignUpActivity)
        // Retrieve phone number from input field
        var number = binding.phoneNumber.text?.trim().toString()
        // Add country code to phone number
        number = "+92$number"
        // Send verification code to provided phone number
        sendVerificationCode(number)
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG", "Auth started")
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
                                this@DoctorSignUpActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DoctorSignUpActivity, error.toString(), Toast.LENGTH_SHORT)
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

        if (binding.accountNumber.text!!.length < 11 || binding.accountNumber.text!!.length > 28) {
            binding.accountNumber.error = getString(R.string.enter_valid_account_number)
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

        if (binding.startTime.text.isNullOrEmpty()) {
            binding.startTime.error = getString(R.string.enter_start_time)
            valid = false
        }

        if (binding.endTime.text.isNullOrEmpty()) {
            binding.endTime.error = getString(R.string.enter_end_time)
            valid = false
        }

        if (binding.fees.text.isNullOrEmpty()) {
            binding.fees.error = getString(R.string.enter_fees)
            valid = false
        }

        if (selectedClinicLatitude == 0.0 && selectedClinicLongitude == 0.0) {
            Toast.makeText(
                this@DoctorSignUpActivity,
                getString(R.string.select_location_again),
                Toast.LENGTH_SHORT
            ).show()
            valid = false
        }

        if (binding.clinicLocation.text.isNullOrEmpty()) {
            binding.clinicLocation.error = getString(R.string.select_location)
            valid = false
        }

        return valid
    }

    private fun checkDoctorProfileExistOrNot(doctorId: String, phoneNo: String) {
        doctorReference.child(doctorId).child(PROFILE_REF)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Handle data retrieval success
                    if (dataSnapshot.exists()) {
                        Toast.makeText(
                            this@DoctorSignUpActivity,
                            getString(R.string.users_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.signUpBtn.isEnabled = true

                    } else {
                        // Data doesn't exist
                        addDataToModel(doctorId, phoneNo)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@DoctorSignUpActivity, databaseError.message, Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addDataToModel(doctorId: String, phoneNo: String) // method for create account
    {
        loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorSignUpActivity)
        val radio: RadioButton = findViewById(binding.genderRadioGroup.checkedRadioButtonId)

        val model = DoctorDetailProfileModel()
        model.id = doctorId
        model.name = binding.name.getText().toString().trim()
        model.phoneNo = phoneNo
        model.email = binding.email.getText().toString().trim()
        model.city = binding.city.getText().toString().trim()
        model.startTime = binding.startTime.getText().toString().trim()
        model.endTime = binding.endTime.getText().toString().trim()
        model.fees = binding.fees.getText().toString().trim()
        model.accountNumber = binding.accountNumber.getText().toString().trim()
        model.clinicLocation = binding.clinicLocation.getText().toString().trim()
        model.clinicLatitude = selectedClinicLatitude
        model.clinicLongitude = selectedClinicLongitude
        model.gender = radio.text.toString()

        uploadImage(model)
    }

    private fun uploadImage(model: DoctorDetailProfileModel) {

        val filepath: StorageReference = storageRef.child("DoctorProfileImages/" + model.id)

        val uploadTask = filepath.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL
            val downloadUrlTask = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnSuccessListener { uri ->
                model.imgUrl = uri.toString()
                addDataToDb(model)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@DoctorSignUpActivity, it.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@DoctorSignUpActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDataToDb(model: DoctorDetailProfileModel) {
        doctorReference.child(model.id).child(PROFILE_REF).setValue(model).addOnSuccessListener {
            getFCMToken(model)
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@DoctorSignUpActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFCMToken(doctorProfileModel: DoctorDetailProfileModel) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Update FCM token in Firestore
            setFCMTokenToDb(token, doctorProfileModel)
        }.addOnFailureListener { exception ->
            // Handle FCM token retrieval failure
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@DoctorSignUpActivity, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFCMTokenToDb(token: String, doctorProfileModel: DoctorDetailProfileModel) {

        val map = HashMap<String, Any>()
        map["fcmToken"] = token
        doctorReference.child(doctorProfileModel.id).child(PROFILE_REF).updateChildren(map)
            .addOnSuccessListener {
                doctorProfileModel.fcmToken = token
                goToDoctorDashBoard(doctorProfileModel)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@DoctorSignUpActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToDoctorDashBoard(model: DoctorDetailProfileModel) {
        appSharedPreferences.put("doctorPhoneNo", model.phoneNo)
        appSharedPreferences.put("doctorUid", model.id)
        appSharedPreferences.put("doctorName", model.name)
        appSharedPreferences.put("doctorEmail", model.email)
        appSharedPreferences.put("doctorImgUrl", model.imgUrl)
        appSharedPreferences.put("doctorAccountNumber", model.accountNumber)
        appSharedPreferences.put("doctorCity", model.city)
        appSharedPreferences.put("doctorGender", model.gender)
        appSharedPreferences.put("doctorClinicLocation", model.clinicLocation)
        appSharedPreferences.put("doctorStartTime", model.startTime)
        appSharedPreferences.put("doctorEndTime", model.endTime)
        appSharedPreferences.put("doctorFees", model.fees)
        appSharedPreferences.put("doctorClinicLatitude", model.clinicLatitude.toFloat())
        appSharedPreferences.put("doctorClinicLongitude", model.clinicLongitude.toFloat())
        appSharedPreferences.put("doctorLogin", true)
        appSharedPreferences.put("is_lang_set", true)


        if ((!appSharedPreferences.getString("doctorPhoneNo")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorUid")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorName")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorEmail")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorAccountNumber")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorImgUrl")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorCity")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorGender")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorClinicLocation")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorStartTime")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorEndTime")
                .isNullOrEmpty() && !appSharedPreferences.getString("doctorFees")
                .isNullOrEmpty() && appSharedPreferences.getFloat("doctorClinicLatitude")
                .toDouble() != 0.0 && appSharedPreferences.getFloat("doctorClinicLongitude")
                .toDouble() != 0.0 && !appSharedPreferences.getString("userType")
                .isNullOrEmpty()) && appSharedPreferences.getBoolean("doctorLogin") && appSharedPreferences.getBoolean(
                "is_lang_set"
            )
        ) {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorSignUpActivity,
                getString(R.string.signup_successfully),
                Toast.LENGTH_SHORT
            ).show()
            startActivity(
                Intent(
                    this@DoctorSignUpActivity, DoctorDashBoardActivity::class.java
                )
            )
            finish()
        } else {

            appSharedPreferences.put("doctorPhoneNo", model.phoneNo)
            appSharedPreferences.put("doctorUid", model.id)
            appSharedPreferences.put("doctorName", model.name)
            appSharedPreferences.put("doctorEmail", model.email)
            appSharedPreferences.put("doctorAccountNumber", model.accountNumber)
            appSharedPreferences.put("doctorImgUrl", model.imgUrl)
            appSharedPreferences.put("doctorCity", model.city)
            appSharedPreferences.put("doctorGender", model.gender)
            appSharedPreferences.put("doctorClinicLocation", model.clinicLocation)
            appSharedPreferences.put("doctorStartTime", model.startTime)
            appSharedPreferences.put("doctorEndTime", model.endTime)
            appSharedPreferences.put("doctorFees", model.fees)
            appSharedPreferences.put("doctorClinicLatitude", model.clinicLatitude.toFloat())
            appSharedPreferences.put("doctorClinicLongitude", model.clinicLongitude.toFloat())
            appSharedPreferences.put("doctorLogin", true)
            appSharedPreferences.put("is_lang_set", true)
            appSharedPreferences.put("userType", "doctor")

            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(
                this@DoctorSignUpActivity,
                getString(R.string.signup_successfully),
                Toast.LENGTH_SHORT
            ).show()
            startActivity(
                Intent(
                    this@DoctorSignUpActivity, DoctorDashBoardActivity::class.java
                )
            )
            finish()
        }
    }
}