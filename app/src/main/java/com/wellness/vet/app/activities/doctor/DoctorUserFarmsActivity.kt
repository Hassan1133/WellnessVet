package com.wellness.vet.app.activities.doctor

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.adapters.DoctorUserFarmsListAdp
import com.wellness.vet.app.databinding.ActivityDoctorUserFarmsBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorAppointmentListModel
import com.wellness.vet.app.models.FarmModel

class DoctorUserFarmsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorUserFarmsBinding
    private lateinit var userFarmDatabaseRef: DatabaseReference
    private lateinit var farmRecyclerAdapter: DoctorUserFarmsListAdp
    private var farmsList = ArrayList<FarmModel>()
    private lateinit var model: DoctorAppointmentListModel
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorUserFarmsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init()
    {
        getDataFromIntent()

        userFarmDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(
                model.uId
            ).child(AppConstants.FARM_REF)

        farmRecyclerAdapter = DoctorUserFarmsListAdp(this@DoctorUserFarmsActivity, farmsList, model)
        binding.recyclerView.adapter = farmRecyclerAdapter

        getFarms()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchList = ArrayList<FarmModel>()
                for (i in farmsList) {
                    if (i.name.lowercase().contains(newText!!.lowercase()) ||
                        i.number.lowercase().contains(newText.lowercase())
                    ) {
                        searchList.add(i)
                    }
                }
                // Update RecyclerView with search results
                farmRecyclerAdapter.updateList(searchList)
                return true
            }
        })
    }

    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("model") as DoctorAppointmentListModel
    }

    private fun getFarms() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorUserFarmsActivity)
        userFarmDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                farmsList.clear()
                snapshot.children.forEach {
                    val model = FarmModel(
                        it.key.toString(),
                        it.child("name").value.toString(),
                        it.child("number").value.toString(),
                    )
                    farmsList.add(model)
                }
                farmRecyclerAdapter.updateList(farmsList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }

            override fun onCancelled(error: DatabaseError) {
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }

        })
    }
}