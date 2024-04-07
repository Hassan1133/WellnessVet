package com.wellness.vet.app.activities.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.ViewPagerAdapter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        getUserNameData()
        permissionUtils.checkAndRequestPermissions()
    }

    private fun getUserNameData() {
        binding.name.text = appSharedPreferences.getString("userName")
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
        MaterialAlertDialogBuilder(this@UserDashBoardActivity, R.style.ThemeOverlay_App_MaterialAlertDialog).setMessage(R.string.logout_message)
            .setCancelable(false).setPositiveButton(R.string.yes) { _, _ ->
                logout() // Perform logout
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        appSharedPreferences.clear()
        val intent = Intent(this@UserDashBoardActivity, UserLoginSignupActivity::class.java)
        startActivity(intent)
        finish()
    }
}