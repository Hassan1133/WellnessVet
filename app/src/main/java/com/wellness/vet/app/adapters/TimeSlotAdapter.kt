package com.wellness.vet.app.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimeSlotAdapter (
    private val context: Context,
    private var slotList: ArrayList<TimeSlotModel>,
    private val slotDate: String,
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
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = Calendar.getInstance().time
            if(slotList[position].status=="available"){
                val fakeList = ArrayList<TimeSlotModel>()
               for (i in 0..<slotList.size){
                   if(i==position){
                       var splitList = slotList[i].slot.split('-')

                       val lastDate = dateFormat.parse(slotDate)
                       val currentDate = dateFormat.parse(dateFormat.format(currentTime))
                       val lastTime = timeFormat.parse(splitList[1].trim().toString())

                       if(lastDate.equals(currentDate) && timeFormat.parse(timeFormat.format(lastTime)).before(timeFormat.parse(timeFormat.format(currentTime)))){
                           Toast.makeText(context,"selected slot time is over please select ",Toast.LENGTH_SHORT).show()
                           fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,false))
                       }
                       else{
                           if(slotList[i].isSelected){
                               fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,false))
                               slotSelectListener.OnTimeSlotSelected(slotList[i].slot,false)
                           }else{
                               fakeList.add(TimeSlotModel(slotList[i].slot,slotList[i].status,true))
                               slotSelectListener.OnTimeSlotSelected(slotList[i].slot,true)
                           }
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