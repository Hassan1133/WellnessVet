package com.wellness.vet.app.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorUserPetsDetailsActivity
import com.wellness.vet.app.models.FarmModel
import com.wellness.vet.app.models.PetModel

class DoctorUserPestsListAdp(
    private val context: Activity,
    private var petsList: List<PetModel>,
    private var userId: String,
    private var date: String,
    private val farmModel: FarmModel
) :
    RecyclerView.Adapter<DoctorUserPestsListAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_pet_view, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petModel = petsList[position]
        holder.petName.text = petModel.name
        holder.petBreed.text = petModel.breed

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DoctorUserPetsDetailsActivity::class.java)
            intent.putExtra("model", petModel)
            intent.putExtra("farmModel", farmModel)
            intent.putExtra("userId", userId)
            intent.putExtra("date", date)
            context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return petsList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petName: TextView = itemView.findViewById(R.id.petName)
        val petBreed: TextView = itemView.findViewById(R.id.petBreed)
    }

    fun updateList(updateList: ArrayList<PetModel>) {
        petsList = updateList
        this.notifyDataSetChanged()
    }
}