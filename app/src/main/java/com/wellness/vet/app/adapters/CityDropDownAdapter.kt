package com.wellness.vet.app.adapters

import android.content.Context
import android.widget.ArrayAdapter
import com.wellness.vet.app.R


class CityDropDownAdapter(context: Context?, private val cityList: List<String>) :
    ArrayAdapter<String>(context!!, R.layout.drop_down_item) {
    override fun getCount(): Int {
        return cityList.size
    }

    override fun getItem(position: Int): String {
        return cityList[position]
    }
}