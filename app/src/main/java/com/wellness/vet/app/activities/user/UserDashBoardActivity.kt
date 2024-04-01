package com.wellness.vet.app.activities.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.ViewPagerAdapter
import com.wellness.vet.app.databinding.ActivityUserDashBoardBinding
import com.wellness.vet.app.fragments.user.UserAppointmentFragment
import com.wellness.vet.app.fragments.user.UserChatFragment
import com.wellness.vet.app.fragments.user.UserFindFragment

class UserDashBoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDashBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listOfFragments =
            listOf(UserChatFragment(), UserFindFragment(), UserAppointmentFragment())
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

    }
}