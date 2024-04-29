package com.wellness.vet.app.fragments.doctor

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.otpview.OTPListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.doctor.DoctorSignUpActivity
import com.wellness.vet.app.databinding.FragmentDoctorLoginBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.NetworkManager
import com.wellness.vet.app.models.DoctorDetailProfileModel
import java.util.concurrent.TimeUnit


class DoctorLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentDoctorLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var doctorsRef: DatabaseReference

    private lateinit var doctorDetailProfileModel: DoctorDetailProfileModel

    private lateinit var loadingDialog: Dialog

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoctorLoginBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        init()
        return binding.root
    }

    private fun init() {
        binding.signUpTxt.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
        doctorDetailProfileModel = DoctorDetailProfileModel()
        doctorsRef = FirebaseDatabase.getInstance().getReference(AppConstants.DOCTOR_REF)
        firebaseAuth = FirebaseAuth.getInstance()
        appSharedPreferences = AppSharedPreferences(requireActivity())

        startOtpVerificationCode()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signUpTxt -> {
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        DoctorSignUpActivity::class.java
                    )
                )
                requireActivity().finish()
            }

            R.id.loginBtn -> {
                // Check network connectivity
                val networkManager = NetworkManager(requireActivity())
                try {
                    val isConnected = networkManager.isNetworkAvailable()
                    if (isConnected) {
                        // Validate user input
                        if (isValid()) {
                            // Perform sign in
                            signIn()
                        }
                    } else {
                        activity?.let {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.please_connect_to_internet),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle network check exception
                    activity?.let {
                        Toast.makeText(
                            requireActivity(), e.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.phoneNumber.text!!.length < 11) {
            binding.phoneNumber.error = getString(R.string.enter_valid_phone_number)
            valid = false
        }

        return valid
    }

    private fun signIn() {

        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
        // Retrieve phone number from input field
        var number = binding.phoneNumber.text?.trim().toString()
        // Add country code to phone number
        number = "+92$number"
        // Send verification code to provided phone number
        sendVerificationCode(number)
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(requireActivity())
            .setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG", "Auth started")
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    checkDoctorExists(
                        task.result.user?.uid.toString()
                    )
                } else {
                    // If sign-in fails, display error message
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(requireActivity(), "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
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
    // Check if LM exists in Firestore
    private fun checkDoctorExists(userId: String) {
        doctorsRef.child(userId).child(AppConstants.PROFILE_REF)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val doctorDetailProfileModel =
                            snapshot.getValue(DoctorDetailProfileModel::class.java)
                        loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
                        getFCMToken(doctorDetailProfileModel!!)
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.account_not_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getFCMToken(doctorProfileModel: DoctorDetailProfileModel) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Update FCM token in Firestore
            setFCMTokenToDb(token, doctorProfileModel)
        }.addOnFailureListener { exception ->
            // Handle FCM token retrieval failure
            activity?.let {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFCMTokenToDb(token: String, doctorProfileModel: DoctorDetailProfileModel) {

        val map = HashMap<String, Any>()
        map["fcmToken"] = token
        doctorsRef.child(doctorProfileModel.id).child(PROFILE_REF).updateChildren(map)
            .addOnSuccessListener {
                doctorProfileModel.fcmToken = token
                goToDoctorDashBoard(doctorProfileModel)
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToDoctorDashBoard(model: DoctorDetailProfileModel) {

        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }

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
            requireActivity(), getString(R.string.login_successfully), Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(requireActivity(), DoctorDashBoardActivity::class.java))
        requireActivity().finish()
    }

}