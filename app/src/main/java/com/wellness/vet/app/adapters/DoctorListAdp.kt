package com.wellness.vet.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserChatActivity
import com.wellness.vet.app.models.DoctorDetailProfileModel

class DoctorListAdp(
    private val context: Context,
    private val doctorList: List<DoctorDetailProfileModel>,
    private val flag: String
) :
    RecyclerView.Adapter<DoctorListAdp.ViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.doctor_list_recycler_design, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views in each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctorDetailProfileModel = doctorList[position]
        holder.doctorName.text = doctorDetailProfileModel.name
        holder.doctorCity.text = doctorDetailProfileModel.city

        holder.itemView.setOnClickListener {
            if (flag == "chat") {
                val intent = Intent(context, UserChatActivity::class.java)
                intent.putExtra("uid", doctorDetailProfileModel.id)
                context.startActivity(intent)
            } else if (flag == "appointment") {

            }
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return doctorList.size
    }

    // ViewHolder class to hold references to views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorName: TextView = itemView.findViewById(R.id.docName)
        val doctorCity: TextView = itemView.findViewById(R.id.docCity)
    }
}