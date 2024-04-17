package com.wellness.vet.app.fragments.doctor

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorDashBoardActivity
import com.wellness.vet.app.activities.doctor.DoctorSignUpActivity
import com.wellness.vet.app.databinding.FragmentDoctorLoginBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.NetworkManager
import com.wellness.vet.app.models.DoctorDetailProfileModel


class DoctorLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentDoctorLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var doctorsRef: DatabaseReference

    private lateinit var doctorDetailProfileModel: DoctorDetailProfileModel

    private lateinit var loadingDialog: Dialog

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
                            loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())
                            signIn(binding.email.text.toString(), binding.password.text.toString())
                        }
                    } else {
                        activity?.let {
                            Toast.makeText(
                                requireActivity(), "Please connect to internet", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle network check exception
                    activity?.let {
                        Toast.makeText(
                            requireActivity(), "Network check failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun isValid(): Boolean {
        var valid = true

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

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Check if LM exists after successful sign-in
                checkDoctorExists(task.result.user!!.uid)
            }
        }.addOnFailureListener { exception ->
            // Handle authentication failure
            LoadingDialog.hideLoadingDialog(loadingDialog)
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if LM exists in Firestore
    private fun checkDoctorExists(userId: String) {
        doctorsRef.child(userId).child(AppConstants.PROFILE_REF)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val doctorDetailProfileModel =
                            snapshot.getValue(DoctorDetailProfileModel::class.java)
                        goToDoctorDashBoard(doctorDetailProfileModel!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
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
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(requireActivity(), "Logged in successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireActivity(), DoctorDashBoardActivity::class.java))
        requireActivity().finish()
    }

}