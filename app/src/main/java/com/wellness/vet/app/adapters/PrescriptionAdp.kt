package com.wellness.vet.app.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.models.PrescriptionModel

class PrescriptionAdp(
    private val context: Activity,
    private var prescriptionList: List<PrescriptionModel>
) :
    RecyclerView.Adapter<PrescriptionAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prescription_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = prescriptionList[position]
        holder.doctorName.text = model.doctorName
        holder.date.text = model.date
        holder.prescription.text = model.prescription
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return prescriptionList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorName: TextView = itemView.findViewById(R.id.doctorName)
        val date: TextView = itemView.findViewById(R.id.date)
        val prescription: TextView = itemView.findViewById(R.id.prescription)
    }

    fun updateList(updateList: ArrayList<PrescriptionModel>) {
        prescriptionList = updateList
        this.notifyDataSetChanged()
    }
}