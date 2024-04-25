package com.wellness.vet.app.fragments.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.messaging.FirebaseMessaging
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserDashBoardActivity
import com.wellness.vet.app.activities.user.UserSignupActivity
import com.wellness.vet.app.databinding.FragmentUserLoginBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.NetworkManager
import com.wellness.vet.app.models.UserProfileModel

class UserLoginFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentUserLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var usersRef: DatabaseReference

    private lateinit var userProfileModel: UserProfileModel

    private lateinit var loadingDialog: Dialog

    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        init()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun init() {
        binding.signUpTxt.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
        userProfileModel = UserProfileModel()
        usersRef = FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF)
        firebaseAuth = FirebaseAuth.getInstance()
        appSharedPreferences = AppSharedPreferences(requireActivity())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signUpTxt -> {
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        UserSignupActivity::class.java
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
                checkUserExists(task.result.user!!.uid)
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
    private fun checkUserExists(userId: String) {

        usersRef.child(userId).child(AppConstants.PROFILE_REF)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfileModel = snapshot.getValue(UserProfileModel::class.java)
                        getFCMToken(userProfileModel!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                    Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getFCMToken(userProfileModel: UserProfileModel) {

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Update FCM token in Firestore
            setFCMTokenToDb(token, userProfileModel)
        }.addOnFailureListener { exception ->
            // Handle FCM token retrieval failure
            activity?.let {
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFCMTokenToDb(token: String, userProfileModel: UserProfileModel) {

        val map = HashMap<String, Any>()
        map["fcmToken"] = token
        usersRef.child(userProfileModel.id).child(PROFILE_REF).updateChildren(map).addOnSuccessListener {
            userProfileModel.fcmToken = token
            goToUserDashBoard(userProfileModel)
        }.addOnFailureListener {
            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToUserDashBoard(userProfileModel: UserProfileModel) {

        if (!isAdded) {
            // Fragment is not attached to an activity
            return
        }

        appSharedPreferences.put("userPhoneNo", userProfileModel.phoneNo)
        appSharedPreferences.put("userUid", userProfileModel.id)
        appSharedPreferences.put("userName", userProfileModel.name)
        appSharedPreferences.put("userCity", userProfileModel.city)
        appSharedPreferences.put("userGender", userProfileModel.gender)
        appSharedPreferences.put("userImgUrl", userProfileModel.imgUrl)
        appSharedPreferences.put("userEmail", userProfileModel.email)
        appSharedPreferences.put("userLogin", true)
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(requireActivity(), "Logged in successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireActivity(), UserDashBoardActivity::class.java))
        requireActivity().finish()
    }
}