package com.wellness.vet.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserAppointmentDetailsActivity
import com.wellness.vet.app.activities.user.UserChatActivity
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

        val userAppointmentListModel = appointmentList[position]

        holder.txtDoctorName.text = userAppointmentListModel.name
        holder.txtDate.text = userAppointmentListModel.date
        holder.txtTime.text = userAppointmentListModel.time

        holder.itemView.setOnClickListener{
            val intent = Intent(context, UserAppointmentDetailsActivity::class.java)
            intent.putExtra("userAppointmentListModel", userAppointmentListModel)
            context.startActivity(intent)
        }
    }

    fun updateList(updateList: ArrayList<UserAppointmentListModel>) {
        appointmentList = updateList
        this.notifyDataSetChanged()
    }
}