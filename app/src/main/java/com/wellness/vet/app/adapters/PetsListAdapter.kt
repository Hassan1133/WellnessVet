package com.wellness.vet.app.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.CreateAppointmentActivity
import com.wellness.vet.app.activities.user.UserChatActivity
import com.wellness.vet.app.activities.user.UserPetDetailsActivity
import com.wellness.vet.app.models.DoctorDetailProfileModel
import com.wellness.vet.app.models.PetModel
import de.hdodenhof.circleimageview.CircleImageView

class PetsListAdapter(
    private val context: Activity,
    private var petsList: List<PetModel>
) :
    RecyclerView.Adapter<PetsListAdapter.ViewHolder>() {

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

        holder.itemView.setOnClickListener{
            val intent = Intent(context, UserPetDetailsActivity::class.java)
            intent.putExtra("model", petModel)
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