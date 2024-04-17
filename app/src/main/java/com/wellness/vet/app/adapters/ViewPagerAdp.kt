package com.wellness.vet.app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wellness.vet.app.fragments.doctor.DoctorLoginFragment
import com.wellness.vet.app.fragments.user.UserLoginFragment

class ViewPagerAdp(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    // Returns the total number of fragments in the ViewPager
    override fun getItemCount(): Int {
        return 2 // Assuming there are 5 fragments
    }

    // Creates and returns the fragment at the specified position
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserLoginFragment()   // Fragment for user login
            1 -> DoctorLoginFragment()    // Fragment for doctor login
            else -> UserLoginFragment() // Default: user login fragment
        }
    }
}
