package com.wellness.vet.app.activities.doctor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.wellness.vet.app.databinding.ActivityDoctorDashBoardBinding
import com.wellness.vet.app.fragments.doctor.DoctorAppointmentFragment
import com.wellness.vet.app.fragments.doctor.DoctorChatFragment
import com.wellness.vet.app.main_utils.AppSharedPreferences

class DoctorDashBoardActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityDoctorDashBoardBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
        getUserNameData()
    }
    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@DoctorDashBoardActivity)
        binding.logoutBtn.setOnClickListener(this)
        binding.profile.setOnClickListener(this)
        val listOfFragments = listOf(DoctorChatFragment(), DoctorAppointmentFragment())
        binding.viewPager.adapter = ViewPagerAdapter(this, listOfFragments)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNavigation.menu.findItem(R.id.chats).isChecked = true
                    1 -> binding.bottomNavigation.menu.findItem(R.id.appointments).isChecked = true

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

                R.id.appointments -> {
                    binding.viewPager.setCurrentItem(1, true)
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
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
        binding.name.text = appSharedPreferences.getString("doctorName")
        Glide.with(applicationContext).load(appSharedPreferences.getString("doctorImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.profile)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog()
            R.id.profile -> startActivity(Intent(this@DoctorDashBoardActivity, ShowDoctorProfileActivity::class.java))
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@DoctorDashBoardActivity, R.style.ThemeOverlay_App_MaterialAlertDialog).setMessage(R.string.logout_message)
            .setCancelable(false).setPositiveButton(R.string.yes) { _, _ ->
                logout() // Perform logout
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        appSharedPreferences.clear()
        val intent = Intent(this@DoctorDashBoardActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}