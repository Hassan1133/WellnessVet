package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.adapters.DoctorListAdp
import com.wellness.vet.app.databinding.ActivityFindDoctorFromListBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorDetailProfileModel


class FindDoctorFromListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindDoctorFromListBinding

    private lateinit var doctorRef: DatabaseReference

    private lateinit var doctorsProfileList: MutableList<DoctorDetailProfileModel>

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindDoctorFromListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        doctorRef = FirebaseDatabase.getInstance().getReference(DOCTOR_REF)
        doctorsProfileList = mutableListOf()
        binding.recyclerView.layoutManager = LinearLayoutManager(this@FindDoctorFromListActivity)
        fetchDoctorList()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText!!)
                return true
            }
        })
    }

    private fun search(newText: String) {
        val searchList = mutableListOf<DoctorDetailProfileModel>()
        for (i in doctorsProfileList) {
            if (i.name.lowercase().contains(newText.lowercase()) || i.city.lowercase()
                    .contains(newText.lowercase())
            ) {
                searchList.add(i)
            }
        }
        // Update RecyclerView with search results
        setDataToRecycler(searchList)
    }

    private fun fetchDoctorList() {

        loadingDialog = LoadingDialog.showLoadingDialog(this@FindDoctorFromListActivity)

        doctorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                doctorsProfileList.clear()

                if (snapshot.exists()) {
                    for (id in snapshot.getChildren()) {
                        val profile: DoctorDetailProfileModel =
                            id.child(PROFILE_REF).getValue(DoctorDetailProfileModel::class.java)!!
                        doctorsProfileList.add(profile)
                    }

                    LoadingDialog.hideLoadingDialog(loadingDialog)

                    setDataToRecycler(doctorsProfileList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@FindDoctorFromListActivity, error.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setDataToRecycler(list: List<DoctorDetailProfileModel>) {
        val flag = intent.getStringExtra("flag")
        if(!flag.isNullOrEmpty())
        {
            val adapter = DoctorListAdp(this@FindDoctorFromListActivity, list, flag)
            binding.recyclerView.adapter = adapter
        }
    }
}