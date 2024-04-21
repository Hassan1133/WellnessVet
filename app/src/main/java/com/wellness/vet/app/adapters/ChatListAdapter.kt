package com.wellness.vet.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.ImageVideoViewerActivity
import com.wellness.vet.app.models.ChatDataModel

class ChatListAdapter(
    private val context: Context,
    private var chatList: ArrayList<ChatDataModel>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatListAdapter.ChatRecyclerViewHolder>() {


    class ChatRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val relativeText: RelativeLayout = itemView.findViewById(R.id.relative_text)
        val relativeImage: RelativeLayout = itemView.findViewById(R.id.relative_image)
        val relativeVideo: RelativeLayout = itemView.findViewById(R.id.relative_video)


        val linearTextFrom: LinearLayout = itemView.findViewById(R.id.linear_txt_message_from)
        val linearTextTo: LinearLayout = itemView.findViewById(R.id.linear_txt_message_to)
        val txtMessageFrom: TextView = itemView.findViewById(R.id.txt_message_from)
        val txtMessageTo: TextView = itemView.findViewById(R.id.txt_message_to)


        val linearImageFrom: LinearLayout = itemView.findViewById(R.id.linear_img_message_from)
        val linearImageTo: LinearLayout = itemView.findViewById(R.id.linear_img_message_to)
        val imgMessageFrom: ImageView = itemView.findViewById(R.id.img_message_from)
        val imgMessageTo: ImageView = itemView.findViewById(R.id.img_message_to)

        val linearVideoFrom: LinearLayout = itemView.findViewById(R.id.linear_vid_message_from)
        val linearVideoTo: LinearLayout = itemView.findViewById(R.id.linear_vid_message_to)
        val vidMessageFrom: ImageView = itemView.findViewById(R.id.vid_message_from)
        val vidMessageTo: ImageView = itemView.findViewById(R.id.vid_message_to)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.single_message_layout, parent, false)
        return ChatRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatRecyclerViewHolder, position: Int) {
        if (chatList[position].type == "text") {
            holder.relativeText.visibility = View.VISIBLE
            holder.relativeImage.visibility = View.GONE
            if (currentUserId == chatList[position].from) {
                holder.linearTextTo.visibility = View.GONE
                holder.linearTextFrom.visibility = View.VISIBLE
                holder.txtMessageFrom.text = chatList[position].message
            } else {
                holder.linearTextTo.visibility = View.VISIBLE
                holder.linearTextFrom.visibility = View.GONE
                holder.txtMessageTo.text = chatList[position].message
            }
        } else if (chatList[position].type == "image") {
            holder.relativeImage.visibility = View.VISIBLE
            holder.relativeText.visibility = View.GONE
            if (currentUserId == chatList[position].from) {
                holder.linearImageTo.visibility = View.GONE
                holder.linearImageFrom.visibility = View.VISIBLE

                Glide.with(context)
                    .load(chatList[position].message)
                    .into(holder.imgMessageFrom)

            } else {
                holder.linearImageTo.visibility = View.VISIBLE
                holder.linearImageFrom.visibility = View.GONE

                Glide.with(context)
                    .load(chatList[position].message)
                    .into(holder.imgMessageTo)
            }
        } else if (chatList[position].type == "video") {
            holder.relativeVideo.visibility = View.VISIBLE
            holder.relativeImage.visibility = View.GONE
            holder.relativeText.visibility = View.GONE
            if (currentUserId == chatList[position].from) {
                holder.linearVideoTo.visibility = View.GONE
                holder.linearVideoFrom.visibility = View.VISIBLE

                Glide.with(context)
                    .load(chatList[position].message)
                    .into(holder.vidMessageFrom)

            } else {
                holder.linearVideoTo.visibility = View.VISIBLE
                holder.linearVideoFrom.visibility = View.GONE

                Glide.with(context)
                    .load(chatList[position].message)
                    .into(holder.vidMessageTo)
            }
        }

        holder.relativeImage.setOnClickListener(View.OnClickListener {
            context.startActivity(Intent(context,ImageVideoViewerActivity::class.java)
                .putExtra("dataType",chatList[position].type)
                .putExtra("dataSource",chatList[position].message))
        })

        holder.relativeVideo.setOnClickListener(View.OnClickListener {
            context.startActivity(Intent(context,ImageVideoViewerActivity::class.java)
                .putExtra("dataType",chatList[position].type)
                .putExtra("dataSource",chatList[position].message))
        })
    }

    fun updateList(updateList: ArrayList<ChatDataModel>) {
        chatList = updateList
        this.notifyDataSetChanged()
    }
}