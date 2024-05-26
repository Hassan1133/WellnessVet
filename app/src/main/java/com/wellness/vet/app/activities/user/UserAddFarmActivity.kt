package com.wellness.vet.app.activities.user

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.FarmListAdapter
import com.wellness.vet.app.adapters.PetsListAdapter
import com.wellness.vet.app.databinding.ActivityUserAddFarmBinding
import com.wellness.vet.app.databinding.AddFarmDialogBinding
import com.wellness.vet.app.databinding.AddPetDialogBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.FarmModel
import com.wellness.vet.app.models.PetModel

class UserAddFarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAddFarmBinding
    private lateinit var userFarmDatabaseRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var farmRecyclerAdapter: FarmListAdapter
    private var farmsList = ArrayList<FarmModel>()
    private lateinit var addFarmDialog: Dialog
    private lateinit var addFarmDialogBinding: AddFarmDialogBinding
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddFarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appSharedPreferences = AppSharedPreferences(this@UserAddFarmActivity)
        userFarmDatabaseRef =
            FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF).child(
                appSharedPreferences.getString("userUid")!!
            ).child(AppConstants.FARM_REF)

        farmRecyclerAdapter = FarmListAdapter(this@UserAddFarmActivity, farmsList)
        binding.recyclerView.adapter = farmRecyclerAdapter

        getFarms()

        binding.addFarmsBtn.setOnClickListener {
            createFarmDialog()
        }

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

    private fun createFarmDialog() {
        addFarmDialogBinding = AddFarmDialogBinding.inflate(LayoutInflater.from(this@UserAddFarmActivity))
        addFarmDialog = Dialog(this@UserAddFarmActivity)
        addFarmDialog.setContentView(addFarmDialogBinding.root)
        addFarmDialog.setCancelable(false)
        addFarmDialog.show()
        addFarmDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addFarmDialogBinding.closeBtn.setOnClickListener {
            addFarmDialog.dismiss()
        }

        addFarmDialogBinding.addFarmBtn.setOnClickListener {
            if (isValid()) {
                addFarmDialogBinding.dialogProgressbar.visibility = View.VISIBLE
                setFarmDataToModel(
                    addFarmDialogBinding.farmName.text.toString(),
                    addFarmDialogBinding.farmNumber.text.toString()
                )
            }
        }
    }

    private fun setFarmDataToModel(
        name: String, number: String
    ) {
        val model = FarmModel()
        model.name = name
        model.number = number
        addFarm(model)
    }

    private fun isValid(): Boolean {
        var valid = true
        if (addFarmDialogBinding.farmName.text.isNullOrEmpty()) {
            addFarmDialogBinding.farmName.error = getString(R.string.enter_valid_farm_name)
            valid = false
        }
        if (addFarmDialogBinding.farmNumber.text.isNullOrEmpty()) {
            addFarmDialogBinding.farmNumber.error = getString(R.string.enter_valid_farm_number)
            valid = false
        }
        return valid
    }

    private fun addFarm(model: FarmModel) {
        model.id = userFarmDatabaseRef.push().key.toString()

        userFarmDatabaseRef.child(model.id).setValue(model).addOnSuccessListener {
            Toast.makeText(
                this@UserAddFarmActivity, getString(R.string.farm_added_successfully), Toast.LENGTH_SHORT
            ).show()
            addFarmDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
            addFarmDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(
                this@UserAddFarmActivity, it.message, Toast.LENGTH_SHORT
            ).show()
            addFarmDialogBinding.dialogProgressbar.visibility = View.INVISIBLE
        }
    }

    private fun getFarms() {
        loadingDialog = LoadingDialog.showLoadingDialog(this@UserAddFarmActivity)
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