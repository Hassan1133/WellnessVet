package com.wellness.vet.app.activities.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.LoginActivity
import com.wellness.vet.app.adapters.ViewPagerAdapter
import com.wellness.vet.app.calls.utils.AuthenticationUtils
import com.wellness.vet.app.calls.utils.ToastUtils
import com.wellness.vet.app.databinding.ActivityUserDashBoardBinding
import com.wellness.vet.app.fragments.user.UserAppointmentFragment
import com.wellness.vet.app.fragments.user.UserChatFragment
import com.wellness.vet.app.fragments.user.UserFindDoctorFragment
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LocationPermissionUtils

class UserDashBoardActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityUserDashBoardBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var permissionUtils: LocationPermissionUtils

    private val MANDATORY_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,  // for VoiceCall and VideoCall
        Manifest.permission.CAMERA // for VoiceCall and VideoCall
    )
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private var mEncodedAuthInfo: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        authenticateSendBird()
        init()
    }

    override fun onResume() {
        super.onResume()
        getUserNameData()
    }

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@UserDashBoardActivity)
        permissionUtils = LocationPermissionUtils(this@UserDashBoardActivity)
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)

        val listOfFragments =
            listOf(UserChatFragment(), UserFindDoctorFragment(), UserAppointmentFragment())
        binding.viewPager.adapter = ViewPagerAdapter(this, listOfFragments)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNavigation.menu.findItem(R.id.chats).isChecked = true
                    1 -> binding.bottomNavigation.menu.findItem(R.id.find).isChecked = true
                    2 -> binding.bottomNavigation.menu.findItem(R.id.appointments).isChecked = true
                }
            }
        })

        // Listen bottom navigation tabs change
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chats -> {
                    binding.viewPager.setCurrentItem(0, true)
                    return@setOnItemSelectedListener true

                }

                R.id.find -> {
                    binding.viewPager.setCurrentItem(1, true)
                    return@setOnItemSelectedListener true
                }

                R.id.appointments -> {
                    binding.viewPager.setCurrentItem(2, true)
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }
        permissionUtils.checkAndRequestPermissions()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        loadFragmentOnNotificationOrOnCreate()
    }

    private fun loadFragmentOnNotificationOrOnCreate() {
        if (intent.hasExtra("appointmentFragment")) {
            val fragmentName = intent.getStringExtra("appointmentFragment")!!
            if (fragmentName == "UserAppointmentFragment") {
                binding.bottomNavigation.selectedItemId = R.id.appointments
            }
        } else {
            binding.bottomNavigation.selectedItemId = R.id.chats
        }
    }
    // Activity result launcher for permission request
    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denial
            // Consider showing a message or taking appropriate action
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with your action
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show rationale to the user, then request permission using launcher
                showPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Request permission directly using launcher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationale(permission: String) {
        // Explain why the app needs permission
        MaterialAlertDialogBuilder(this)
            .setMessage("This app needs notification permission to...") // Provide reason
            .setPositiveButton("Grant") { _, _ -> launcher.launch(permission) }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getUserNameData() {
        binding.name.text = appSharedPreferences.getString("userName")
        Glide.with(applicationContext).load(appSharedPreferences.getString("userImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profile)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog()
            R.id.profile -> {
                startActivity(
                    Intent(
                        this@UserDashBoardActivity,
                        ShowUserProfileActivity::class.java
                    )
                )
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(
            this@UserDashBoardActivity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).setMessage(R.string.logout_message)
            .setCancelable(false).setPositiveButton(R.string.yes) { _, _ ->
                logout() // Perform logout
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        appSharedPreferences.clear()
        val intent = Intent(this@UserDashBoardActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun authenticateSendBird() {
        Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre1")
        val intent = intent
//        if (intent != null) {
            val data = intent.data
            if (data != null) {
                Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre2")
                val scheme = data.scheme
                if (scheme != null && scheme == "sendbird") {
                    Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre3")
                    mEncodedAuthInfo = data.host
                    if (!TextUtils.isEmpty(mEncodedAuthInfo)) {
                        Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre4")
                        AuthenticationUtils.authenticateWithEncodedAuthInfo(
                            this@UserDashBoardActivity,
                            mEncodedAuthInfo
                        ) { isSuccess: Boolean, hasInvalidValue: Boolean ->
                            if (isSuccess) {
                                Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre")

                            } else {
                                if (hasInvalidValue) {
                                    ToastUtils.showToast(
                                        this@UserDashBoardActivity,
                                        getString(R.string.calls_invalid_deep_link)
                                    )
                                } else {
                                    ToastUtils.showToast(
                                        this@UserDashBoardActivity,
                                        getString(R.string.calls_deep_linking_to_authenticate_failed)
                                    )
                                }
                                finish()
                            }
                        }
                    }else{
                        Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre5")
                        AuthenticationUtils.autoAuthenticate(this@UserDashBoardActivity) { userId: String? ->
                            if (!TextUtils.isEmpty(userId)) {
//                                user login successful
                                Log.d("TAGSENDBIRD", "authenticateSendBird: successful login util")

                            } else {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    AuthenticationUtils.authenticate(
                                        this@UserDashBoardActivity, currentUser.uid, ""
                                    ) { isSuccess: Boolean ->
                                        if (isSuccess) {
//                                            user login successful
                                            Log.d("TAGSENDBIRD", "authenticateSendBird: successful login userid")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                Log.d("TAGSENDBIRD", "authenticateSendBird: successful login pre5")
                AuthenticationUtils.autoAuthenticate(this@UserDashBoardActivity) { userId: String? ->
                    if (!TextUtils.isEmpty(userId)) {
//                                user login successful
                        Log.d("TAGSENDBIRD", "authenticateSendBird: successful login util")

                    } else {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            AuthenticationUtils.authenticate(
                                this@UserDashBoardActivity, currentUser.uid, ""
                            ) { isSuccess: Boolean ->
                                if (isSuccess) {
//                                            user login successful
                                    Log.d("TAGSENDBIRD", "authenticateSendBird: successful login userid")
                                }
                            }
                        }
                    }
                }
            }
//        }
    }

    private fun checkPermissions() {
        val permissions =
            ArrayList<String>(listOf<String>(*MANDATORY_PERMISSIONS))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val deniedPermissions = ArrayList<String>()
        for (permission in permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.size > 0) {
            requestPermissions(
                    deniedPermissions.toTypedArray<String>(),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            var allowed = true
            for (result in grantResults) {
                allowed = allowed && result == PackageManager.PERMISSION_GRANTED
            }
            if (!allowed) {
                ToastUtils.showToast(this, "Permission denied.")
            }
        }
    }

}