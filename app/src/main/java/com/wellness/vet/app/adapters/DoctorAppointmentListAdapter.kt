package com.wellness.vet.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorAppointmentDetailsActivity
import com.wellness.vet.app.activities.user.UserAppointmentDetailsActivity
import com.wellness.vet.app.models.DoctorAppointmentListModel
import com.wellness.vet.app.models.UserAppointmentListModel

class DoctorAppointmentListAdapter(
    val context: Context,
    private var appointmentList: ArrayList<DoctorAppointmentListModel>
) : RecyclerView.Adapter<DoctorAppointmentListAdapter.DoctorAppointmentListViewHolder>() {


    class DoctorAppointmentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDoctorName: TextView = itemView.findViewById(R.id.txtDoctorName)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DoctorAppointmentListViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.single_appointment_layout, parent, false)
        return DoctorAppointmentListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(holder: DoctorAppointmentListViewHolder, position: Int) {

        val doctorAppointmentListModel = appointmentList[position]

        holder.txtDoctorName.text = doctorAppointmentListModel.name
        holder.txtDate.text = doctorAppointmentListModel.date
        holder.txtTime.text = doctorAppointmentListModel.time

        if (doctorAppointmentListModel.appointmentStatus == "booked") {
            holder.statusIcon.setImageResource(R.drawable.appoint_done)
        } else if (doctorAppointmentListModel.appointmentStatus == "cancelled") {
            holder.statusIcon.setImageResource(R.drawable.cancelled)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DoctorAppointmentDetailsActivity::class.java)
            intent.putExtra("doctorAppointmentListModel", doctorAppointmentListModel)
            context.startActivity(intent)
        }
    }

    fun updateList(updateList: ArrayList<DoctorAppointmentListModel>) {
        appointmentList = updateList
        this.notifyDataSetChanged()
    }
}