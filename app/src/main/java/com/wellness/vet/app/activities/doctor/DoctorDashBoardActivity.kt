package com.wellness.vet.app.activities.doctor

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

    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@DoctorDashBoardActivity)
        binding.logoutBtn.setOnClickListener(this)
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

        getUserNameData()
    }

    private fun getUserNameData() {
        binding.name.text = appSharedPreferences.getString("doctorName")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.logoutBtn -> showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@DoctorDashBoardActivity).setMessage(R.string.logout_message)
            .setCancelable(false).setPositiveButton(R.string.yes) { _, _ ->
                logout() // Perform logout
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        appSharedPreferences.clear()
        val intent = Intent(this@DoctorDashBoardActivity, DoctorLoginSignupActivity::class.java)
        startActivity(intent)
        finish()
    }
}