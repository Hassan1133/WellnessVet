package com.wellness.vet.app.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.PetsActivity
import com.wellness.vet.app.models.FarmModel

class FarmListAdapter (
    private val context: Activity,
    private var farmsList: List<FarmModel>
) :
    RecyclerView.Adapter<FarmListAdapter.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_pet_view, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farmModel = farmsList[position]
        holder.petName.text = farmModel.name
        holder.petBreed.text = farmModel.number

        holder.itemView.setOnClickListener{
            val intent = Intent(context, PetsActivity::class.java)
            intent.putExtra("model", farmModel)
            context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return farmsList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petName: TextView = itemView.findViewById(R.id.petName)
        val petBreed: TextView = itemView.findViewById(R.id.petBreed)
    }

    fun updateList(updateList: ArrayList<FarmModel>) {
        farmsList = updateList
        this.notifyDataSetChanged()
    }
}