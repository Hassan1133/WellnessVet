package com.wellness.vet.app.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wellness.vet.app.R
import com.wellness.vet.app.interfaces.TimeSlotSelectListener
import com.wellness.vet.app.models.ChatDataModel
import com.wellness.vet.app.models.TimeSlotModel

class TimeSlotAdapter (
    private val context: Context,
    private var slotList: ArrayList<TimeSlotModel>,
    private val slotSelectListener: TimeSlotSelectListener
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>(){


    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val relativeBg: RelativeLayout = itemView.findViewById(R.id.relativeBg)
        val txtTimeSlot: TextView = itemView.findViewById(R.id.txtTimeSlot)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.single_time_slot_layout, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun getItemCount(): Int {
        return slotList.size
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.txtTimeSlot.text = slotList[position].slot
        if(slotList[position].status=="available"){
            holder.relativeBg.setBackgroundColor(Color.WHITE)
        }else{
            holder.relativeBg.setBackgroundColor(Color.GRAY)
        }
        if(slotList[position].isSelected){
            holder.relativeBg.setBackgroundColor(Color.GREEN)
        }
        holder.relativeBg.setOnClickListener(View.OnClickListener {
            if(slotList[position].status=="available"){
                val fakeList = ArrayList<TimeSlotModel>()
               for (i in 0..<slotList.size){
                   if(i==position){
                       if(slotList[i].isSelected){
                           fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,false))
                           slotSelectListener.OnTimeSlotSelected(slotList[i].slot,false)
                       }else{
                           fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,true))
                           slotSelectListener.OnTimeSlotSelected(slotList[i].slot,true)
                       }
                   }else{
                       fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,false))
                   }
               }
                slotList = fakeList
                this.notifyDataSetChanged()
            }else{
                Toast.makeText(context,"This Slot is already taken, Choose another",Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateList(updateList: ArrayList<TimeSlotModel>) {
        slotList = updateList
        this.notifyDataSetChanged()
    }

}