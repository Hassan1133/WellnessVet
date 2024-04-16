package com.wellness.vet.app.fragments.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.FindDoctorFromListActivity
import com.wellness.vet.app.databinding.FragmentUserAppointmentBinding


class UserAppointmentFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentUserAppointmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAppointmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        init()
        return binding.root
    }

    private fun init()
    {
        binding.addAppointmentBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.addAppointmentBtn -> goToFindDoctorFromListActivity()
        }
    }

    private fun goToFindDoctorFromListActivity() {
        val intent = Intent(requireActivity(), FindDoctorFromListActivity::class.java)
        intent.putExtra("flag", "appointment")
        startActivity(intent)
    }
}