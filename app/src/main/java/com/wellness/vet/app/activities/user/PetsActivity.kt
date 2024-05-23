package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.PetsListAdapter
import com.wellness.vet.app.databinding.ActivityPetsBinding
import com.wellness.vet.app.databinding.AddPetDialogBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.PetModel

class PetsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPetsBinding
    private lateinit var userPetDatabaseRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var petRecyclerAdapter: PetsListAdapter
    private var petsList = ArrayList<PetModel>()
    private lateinit var addPetDialog: Dialog
    private lateinit var addPetDialogBinding: AddPetDialogBinding
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appSharedPreferences = AppSharedPreferences(this@PetsActivity)
        userPetDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(
                appSharedPreferences.getString("userUid")!!
            ).child(AppConstants.PET_REF)

        petRecyclerAdapter = PetsListAdapter(this@PetsActivity, petsList)
        binding.recyclerView.adapter = petRecyclerAdapter

        getPets()

        binding.addPetsBtn.setOnClickListener {
            createPetDialog()
        }

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

    private fun createPetDialog() {
        addPetDialogBinding = AddPetDialogBinding.inflate(LayoutInflater.from(this@PetsActivity))
        addPetDialog = Dialog(this@PetsActivity)
        addPetDialog.setContentView(addPetDialogBinding.root)
        addPetDialog.setCancelable(false)
        addPetDialog.show()
        addPetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addPetDialogBinding.closeBtn.setOnClickListener {
            addPetDialog.dismiss()
        }

        addPetDialogBinding.addPetBtn.setOnClickListener {
            if (isValid()) {
                addPetDialogBinding.dialogProgressbar.visibility = View.VISIBLE
                val radio: RadioButton =
                    addPetDialog.findViewById(addPetDialogBinding.genderRadioGroup.checkedRadioButtonId)
                setPetDataToModel(
                    addPetDialogBinding.name.text.toString(),
                    addPetDialogBinding.age.text.toString(),
                    addPetDialogBinding.breed.text.toString(),
                    radio.text.toString()
                )
            }
        }
    }

    private fun setPetDataToModel(
        name: String, age: String, breed: String, gender: String
    ) {
        val petModel = PetModel()
        petModel.name = name
        petModel.age = age
        petModel.breed = breed
        petModel.gender = gender
        addPet(petModel)
    }

    private fun isValid(): Boolean {
        var valid = true
        if (addPetDialogBinding.name.text.isNullOrEmpty()) {
            addPetDialogBinding.name.error = getString(R.string.enter_valid_pet_name)
            valid = false
        }
        if (addPetDialogBinding.age.text.isNullOrEmpty()) {
            addPetDialogBinding.age.error = getString(R.string.enter_valid_pet_age)
            valid = false
        }
        if (addPetDialogBinding.breed.text.isNullOrEmpty()) {
            addPetDialogBinding.breed.error = getString(R.string.enter_valid_pet_breed)
            valid = false
        }
        return valid
    }

    private fun addPet(petModel: PetModel) {
        petModel.id = userPetDatabaseRef.push().key.toString()

        userPetDatabaseRef.child(petModel.id).setValue(petModel).addOnSuccessListener {
            Toast.makeText(
                this@PetsActivity, getString(R.string.pet_added_successfully), Toast.LENGTH_SHORT
            ).show()
            addPetDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
            addPetDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(
                this@PetsActivity, it.message, Toast.LENGTH_SHORT
            ).show()
            addPetDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
        }
    }

    private fun getPets() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@PetsActivity)
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