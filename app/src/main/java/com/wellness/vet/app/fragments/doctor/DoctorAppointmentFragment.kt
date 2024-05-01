package com.wellness.vet.app.fragments.doctor

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.UserAppointmentListAdapter
import com.wellness.vet.app.databinding.FragmentDoctorAppointmentBinding
import com.wellness.vet.app.databinding.FragmentUserAppointmentBinding
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.UserAppointmentListModel

class DoctorAppointmentFragment : Fragment() {

    private lateinit var binding: FragmentDoctorAppointmentBinding
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoctorAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val dataBase = Firebase.database
        val appointmentDbRef = dataBase.getReference("appointments")
        val profileDbRef = dataBase.getReference("Users")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val appointmentList = ArrayList<UserAppointmentListModel>()
        val appointmentListAdapter = UserAppointmentListAdapter(requireContext(), appointmentList)
        binding.recyclerView.adapter = appointmentListAdapter


        if (currentUser != null) {
            val userUid = currentUser.uid

            loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())

            appointmentDbRef.child(userUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointmentList.clear()
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            profileDbRef.child(ds.key.toString()).child("Profile")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        ds.children.forEach {

                                            val appointmentDetail = UserAppointmentListModel(
                                                snapshot.child("name").value.toString(),
                                                it.child("date").value.toString(),
                                                it.child("time").value.toString()
                                            )
                                            appointmentList.add(appointmentDetail)
                                            appointmentListAdapter.updateList(appointmentList)
                                            LoadingDialog.hideLoadingDialog(loadingDialog)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        LoadingDialog.hideLoadingDialog(loadingDialog)
                                    }

                                })
                        }
                    } else {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                }

            })

            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val searchList = ArrayList<UserAppointmentListModel>()
                    for (i in appointmentList) {
                        if (i.name.lowercase().contains(newText!!.lowercase())
                        ) {
                            searchList.add(i)
                        }
                    }
                    // Update RecyclerView with search results
                    appointmentListAdapter.updateList(searchList)
                    return true
                }
            })

        }
    }

}