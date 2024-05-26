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
import com.wellness.vet.app.adapters.DoctorUserPestsListAdp
import com.wellness.vet.app.databinding.ActivityDoctorUserPetsBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.DoctorAppointmentListModel
import com.wellness.vet.app.models.FarmModel
import com.wellness.vet.app.models.PetModel

class DoctorUserPetsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorUserPetsBinding
    private lateinit var model: FarmModel
    private lateinit var appointmentModel: DoctorAppointmentListModel
    private lateinit var userPetDatabaseRef: DatabaseReference
    private lateinit var loadingDialog: Dialog
    private lateinit var petRecyclerAdapter: DoctorUserPestsListAdp
    var petsList = ArrayList<PetModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorUserPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getDataFromIntent()

        userPetDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(
                appointmentModel.uId
            ).child(AppConstants.FARM_REF).child(model.id).child(AppConstants.PET_REF)

        petRecyclerAdapter =
            DoctorUserPestsListAdp(this@DoctorUserPetsActivity, petsList, appointmentModel.uId, appointmentModel.date, model)
        binding.recyclerView.adapter = petRecyclerAdapter

        getPets()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchList = ArrayList<PetModel>()
                for (i in petsList) {
                    if (i.name.lowercase().contains(newText!!.lowercase()) ||
                        i.breed.lowercase().contains(newText.lowercase()) ||
                        i.age.lowercase().contains(newText.lowercase()) ||
                        i.gender.lowercase().contains(newText.lowercase())
                    ) {
                        searchList.add(i)
                    }
                }
                // Update RecyclerView with search results
                petRecyclerAdapter.updateList(searchList)
                return true
            }
        })
    }

    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("model") as FarmModel
        appointmentModel = intent.getSerializableExtra("appointmentModel") as DoctorAppointmentListModel
    }

    private fun getPets() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@DoctorUserPetsActivity)
        userPetDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                petsList.clear()
                snapshot.children.forEach {
                    val petModel = PetModel(
                        it.key.toString(),
                        it.child("name").value.toString(),
                        it.child("gender").value.toString(),
                        it.child("age").value.toString(),
                        it.child("breed").value.toString()
                    )
                    petsList.add(petModel)
                }
                petRecyclerAdapter.updateList(petsList)
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }

            override fun onCancelled(error: DatabaseError) {
                LoadingDialog.hideLoadingDialog(loadingDialog)
            }

        })
    }
}