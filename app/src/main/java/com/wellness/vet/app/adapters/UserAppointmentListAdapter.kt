package com.wellness.vet.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.models.ChatDataModel
import com.wellness.vet.app.models.UserAppointmentListModel

class UserAppointmentListAdapter(
    val context : Context,
    private var appointmentList : ArrayList<UserAppointmentListModel>
    ) : RecyclerView.Adapter<UserAppointmentListAdapter.UserAppointmentListViewHolder>(){


    class UserAppointmentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDoctorName: TextView = itemView.findViewById(R.id.txtDoctorName)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAppointmentListViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.single_appointment_layout, parent, false)
        return UserAppointmentListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(holder: UserAppointmentListViewHolder, position: Int) {
        holder.txtDoctorName.text = appointmentList[position].name
        holder.txtDate.text = appointmentList[position].date
        holder.txtTime.text = appointmentList[position].time
    }

    fun updateList(updateList: ArrayList<UserAppointmentListModel>) {
        appointmentList = updateList
        this.notifyDataSetChanged()
    }
}