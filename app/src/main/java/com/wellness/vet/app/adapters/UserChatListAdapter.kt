package com.wellness.vet.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.UserChatActivity
import com.wellness.vet.app.models.ChatDataModel
import com.wellness.vet.app.models.UserChatListModel

class UserChatListAdapter(
    private val context: Context,
    private var userChatList: ArrayList<UserChatListModel>
) : RecyclerView.Adapter<UserChatListAdapter.ChatListRecyclerViewHolder>() {


    class ChatListRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtDoctorName: TextView = itemView.findViewById(R.id.txt_doctor_name)
        val txtDoctorTime: TextView = itemView.findViewById(R.id.txt_doctor_time)
        val txtDoctorFees: TextView = itemView.findViewById(R.id.txt_doctor_fees)
        val btnChat: RelativeLayout = itemView.findViewById(R.id.btn_chat)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListRecyclerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.single_doctor_chat_layout, parent, false)
        return ChatListRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userChatList.size
    }

    override fun onBindViewHolder(holder: ChatListRecyclerViewHolder, position: Int) {
        holder.txtDoctorName.text = userChatList[position].name
        holder.txtDoctorTime.text = userChatList[position].time
        holder.txtDoctorFees.text = userChatList[position].fees
        holder.btnChat.setOnClickListener(View.OnClickListener {
            context.startActivity(Intent(context,UserChatActivity::class.java).putExtra("uid",userChatList[position].uid))
        })
    }
    fun updateList(updateList: ArrayList<UserChatListModel>) {
        userChatList = updateList
        this.notifyDataSetChanged()
    }
}