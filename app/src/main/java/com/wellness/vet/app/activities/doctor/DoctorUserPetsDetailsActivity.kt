package com.wellness.vet.app.activities.doctor

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.PrescriptionAdp
import com.wellness.vet.app.databinding.ActivityDoctorUserPetsDetailsBinding
import com.wellness.vet.app.databinding.AddPrescriptionDialogBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.models.PetModel
import com.wellness.vet.app.models.PrescriptionModel

class DoctorUserPetsDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorUserPetsDetailsBinding
    private lateinit var model: PetModel
    private lateinit var userId: String
    private lateinit var date: String
    private lateinit var addPrescriptionDialog: Dialog
    private lateinit var addPrescriptionDialogBinding: AddPrescriptionDialogBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var userProfilePetPrescriptionRef: DatabaseReference
    private lateinit var prescriptionRecyclerAdapter: PrescriptionAdp
    private var prescriptionList = ArrayList<PrescriptionModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorUserPetsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        getDataFromIntent()
        appSharedPreferences = AppSharedPreferences(this@DoctorUserPetsDetailsActivity)
        userProfilePetPrescriptionRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(userId)
                .child(AppConstants.PET_REF).child(model.id)
                .child(AppConstants.PRESCRIPTION_REF)
        binding.addPerBtn.setOnClickListener {
            createPrescriptionDialog()
        }
        prescriptionRecyclerAdapter =
            PrescriptionAdp(this@DoctorUserPetsDetailsActivity, prescriptionList)
        binding.recyclerView.adapter = prescriptionRecyclerAdapter

        getPrescriptionData()
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
                    this@DoctorUserPetsDetailsActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun createPrescriptionDialog() {
        addPrescriptionDialogBinding =
            AddPrescriptionDialogBinding.inflate(LayoutInflater.from(this@DoctorUserPetsDetailsActivity))
        addPrescriptionDialog = Dialog(this@DoctorUserPetsDetailsActivity)
        addPrescriptionDialog.setContentView(addPrescriptionDialogBinding.root)
        addPrescriptionDialog.setCancelable(false)
        addPrescriptionDialog.show()
        addPrescriptionDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addPrescriptionDialogBinding.closeBtn.setOnClickListener {
            addPrescriptionDialog.dismiss()
        }

        addPrescriptionDialogBinding.addPrescriptionBtn.setOnClickListener {
            if (isValid()) {
                addPrescriptionDialogBinding.dialogProgressbar.visibility = View.VISIBLE
                setPrescriptionDataToModel(
                    appSharedPreferences.getString("doctorName")!!,
                    date,
                    addPrescriptionDialogBinding.prescription.text.toString(),
                )
            }
        }
    }

    private fun setPrescriptionDataToModel(doctorName: String, date: String, prescription: String) {
        val model = PrescriptionModel()
        model.doctorName = doctorName
        model.date = date
        model.prescription = prescription

        addToDb(model)
    }

    private fun addToDb(model: PrescriptionModel) {
        model.id = userProfilePetPrescriptionRef.push().key.toString()
        userProfilePetPrescriptionRef.child(model.id).setValue(model).addOnSuccessListener {
            Toast.makeText(
                this@DoctorUserPetsDetailsActivity, "Prescription Added", Toast.LENGTH_SHORT
            ).show()
            addPrescriptionDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
            addPrescriptionDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(this@DoctorUserPetsDetailsActivity, it.message, Toast.LENGTH_SHORT)
                .show()
            addPrescriptionDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
        }
    }

    private fun getDataFromIntent() {
        model = intent.getSerializableExtra("model") as PetModel
        userId = intent.getStringExtra("userId")!!
        date = intent.getStringExtra("date")!!
        binding.petName.text = model.name
        binding.petAge.text = model.age
        binding.petBreed.text = model.breed
        binding.petGender.text = model.gender
    }

    private fun isValid(): Boolean {
        var valid = true
        if (addPrescriptionDialogBinding.prescription.text.isNullOrEmpty()) {
            addPrescriptionDialogBinding.prescription.error =
                getString(R.string.enter_valid_prescription)
            valid = false
        }
        return valid
    }
}