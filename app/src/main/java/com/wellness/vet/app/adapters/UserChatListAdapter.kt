package com.wellness.vet.app.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.doctor.DoctorChatActivity
import com.wellness.vet.app.activities.user.UserChatActivity
import com.wellness.vet.app.models.UserChatListModel
import de.hdodenhof.circleimageview.CircleImageView

class UserChatListAdapter(
    private val context: Context,
    private var userChatList: ArrayList<UserChatListModel>,
    private val flag : String
) : RecyclerView.Adapter<UserChatListAdapter.ChatListRecyclerViewHolder>() {


    class ChatListRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtDoctorName: TextView = itemView.findViewById(R.id.docName)
        val txtDoctorCity: TextView = itemView.findViewById(R.id.docCity)
        val doctorImage: CircleImageView = itemView.findViewById(R.id.docImage)
        val active: ImageView = itemView.findViewById(R.id.active)

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
        holder.txtDoctorCity.text = userChatList[position].city
        Glide.with(context).load(userChatList[position].imgUrl)
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(holder.doctorImage)

        if (userChatList[position].chatStatus == "unseen") {
            holder.active.visibility = View.VISIBLE
        }
        else if (userChatList[position].chatStatus == "seen") {
            holder.active.visibility = View.GONE
        }
        Log.d("TAGStatus", "onBindViewHolder: ${userChatList[position].chatStatus}")

        holder.itemView.setOnClickListener(View.OnClickListener {
            if(flag=="user"){
                context.startActivity(
                    Intent(context, UserChatActivity::class.java).putExtra(
                        "uid",
                        userChatList[position].uid
                    ).putExtra("name", userChatList[position].name)
                        .putExtra("imgUrl", userChatList[position].imgUrl)
                )
            }else if(flag == "doctor"){
                context.startActivity(
                    Intent(context, DoctorChatActivity::class.java).putExtra(
                        "uid",
                        userChatList[position].uid
                    ).putExtra("name", userChatList[position].name)
                        .putExtra("imgUrl", userChatList[position].imgUrl)
                )
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateList: ArrayList<UserChatListModel>) {
        userChatList = updateList
        this.notifyDataSetChanged()
    }
}