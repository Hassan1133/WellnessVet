package com.wellness.vet.app.main_utils

import android.content.Context
import android.content.SharedPreferences

class AppSharedPreferences(context: Context) {

    private val preferencesName = "wellness_vet_app_pref"
    private var sharedPref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPref = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    fun put(key: String, value: String) {
        editor.putString(key, value)
            .apply()
    }

    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value)
            .apply()
    }

    fun put(key: String, value: Float) {
        editor.putFloat(key, value)
            .apply()
    }

    fun put(key: String, value: Int) {
        editor.putInt(key, value)
            .apply()
    }

    fun getInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getFloat(key: String): Float {
        return sharedPref.getFloat(key, 0.0f)
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun getString(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun clear() {
        editor.clear()
            .apply()
    }

}