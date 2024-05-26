package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.adapters.PrescriptionAdp
import com.wellness.vet.app.databinding.ActivityUserPetDetailsBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.models.FarmModel
import com.wellness.vet.app.models.PetModel
import com.wellness.vet.app.models.PrescriptionModel

class UserPetDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserPetDetailsBinding
    private lateinit var model: PetModel
    private lateinit var farmModel: FarmModel
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var userProfilePetPrescriptionRef: DatabaseReference
    private lateinit var prescriptionRecyclerAdapter: PrescriptionAdp
    private var prescriptionList = ArrayList<PrescriptionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPetDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getDataFromIntent()

        appSharedPreferences = AppSharedPreferences(this@UserPetDetailsActivity)
        userProfilePetPrescriptionRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(
                appSharedPreferences.getString("userUid")!!
            ).child(AppConstants.FARM_REF).child(farmModel.id).child(AppConstants.PET_REF).child(model.id)
                .child(AppConstants.PRESCRIPTION_REF)

        prescriptionRecyclerAdapter =
            PrescriptionAdp(this@UserPetDetailsActivity, prescriptionList)
        binding.recyclerView.adapter = prescriptionRecyclerAdapter

        getPrescriptionData()
    }

    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("model") as PetModel
        farmModel = intent.getSerializableExtra("farmModel") as FarmModel
        binding.petName.text = model.name
        binding.petAge.text = model.age
        binding.petBreed.text = model.breed
        binding.petGender.text = model.gender
    }

    private fun getPrescriptionData() {
        userProfilePetPrescriptionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                prescriptionList.clear()
                snapshot.children.forEach {
                    val prescriptionModel = PrescriptionModel(
                        it.child("id").value.toString(),
                        it.child("doctorName").value.toString(),
                        it.child("date").value.toString(),
                        it.child("prescription").value.toString(),
                    )

                    prescriptionList.add(prescriptionModel)
                }
                prescriptionRecyclerAdapter.updateList(prescriptionList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserPetDetailsActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
}