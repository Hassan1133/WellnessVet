package com.wellness.vet.app.activities.doctor

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.otpview.OTPListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserLoginSignupActivity
import com.wellness.vet.app.databinding.ActivityDoctorLoginSignupBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorDetailProfileModel
import java.util.concurrent.TimeUnit

class DoctorLoginSignupActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityDoctorLoginSignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var loadingDialog: Dialog
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var doctorRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase authentication
        init()

    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        binding.loginBtn.setOnClickListener(this)
        binding.loginAsUserBtn.setOnClickListener(this)
        appSharedPreferences = AppSharedPreferences(this@DoctorLoginSignupActivity)
        doctorRef = FirebaseDatabase.getInstance().getReference(DOCTOR_REF)

        // Set up callbacks for phone number verification
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
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
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

    override fun onClick(v: View?) {
        // Handle click events
        when (v?.id) {
            R.id.loginBtn -> {
                if (isValid()) {
                    loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorLoginSignupActivity)
                    login()
                }
            }
            R.id.loginAsUserBtn -> {
                startActivity(Intent(this@DoctorLoginSignupActivity, UserLoginSignupActivity::class.java))
                finish()
            }
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.phoneNumber.text.isNullOrEmpty()) {
            binding.phoneNumber.error = "enter valid number"
            valid = false
        }

        return valid
    }

    private fun login() {
        // Retrieve phone number from input field
        var number = binding.phoneNumber.text?.trim().toString()
        // Add country code to phone number
        number = "+92$number"
        // Send verification code to provided phone number
        sendVerificationCode(number)
    }

    // Send verification code to the provided phone number
    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG", "Auth started")
    }

    // Sign in with phone authentication credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // If sign-in is successful, start DoctorProfileActivity
                appSharedPreferences.put("doctorPhoneNo", task.result.user?.phoneNumber.toString())
                appSharedPreferences.put("doctorUid", task.result.user?.uid.toString())
                appSharedPreferences.put("doctorLogin", true)

                checkDoctorProfileExistOrNot(task.result.user?.uid.toString())

            } else {
                // If sign-in fails, display error message
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkDoctorProfileExistOrNot(doctorId: String) {
        doctorRef.child(doctorId).child(PROFILE_REF)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Handle data retrieval success
                    if (dataSnapshot.exists()) {

                       val detailProfileModel : DoctorDetailProfileModel? = dataSnapshot.getValue(DoctorDetailProfileModel::class.java)
                        getDoctorProfileData(detailProfileModel!!)
                        appSharedPreferences.put("doctorProfileAdded", true)
                        appSharedPreferences.put("doctorTimeAdded", true)
                        startActivity(
                            Intent(
                                this@DoctorLoginSignupActivity, DoctorDashBoardActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        // Data doesn't exist
                        startActivity(
                            Intent(
                                this@DoctorLoginSignupActivity, DoctorProfileActivity::class.java
                            )
                        )
                        finish()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@DoctorLoginSignupActivity, databaseError.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getDoctorProfileData(detailProfileModel: DoctorDetailProfileModel) {
        appSharedPreferences.put("doctorName", detailProfileModel.name)
        appSharedPreferences.put("doctorCity", detailProfileModel.city)
        appSharedPreferences.put("doctorGender", detailProfileModel.gender)
        appSharedPreferences.put("doctorClinicLocation", detailProfileModel.clinicLocation)
        appSharedPreferences.put("doctorStartTime", detailProfileModel.startTime)
        appSharedPreferences.put("doctorEndTime", detailProfileModel.endTime)
        appSharedPreferences.put("doctorFees", detailProfileModel.fees)
    }
}