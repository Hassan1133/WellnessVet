package com.wellness.vet.app.activities.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.ViewPagerAdp
import com.wellness.vet.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        // Initialize the activity components
        init()
        // Set the content view to the root layout of the inflated binding
        setContentView(binding.root)
    }

    private fun init() {
        // Create adapter for ViewPager
        val adapter = ViewPagerAdp(supportFragmentManager, lifecycle)
        // Get reference to ViewPager
        val viewPager: ViewPager2 = binding.viewPager
        // Set adapter to ViewPager
        viewPager.adapter = adapter
        // Get reference to TabLayout
        val tabs: TabLayout = binding.tabsLayout
        // Attach TabLayout to ViewPager
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            // Set tab text based on position
            when (position) {
                0 -> tab.text = getString(R.string.user_tab)
                1 -> tab.text = getString(R.string.doctor_tab)
            }
        }.attach()  
    }

}