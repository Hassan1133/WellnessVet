package com.wellness.vet.app.main_utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkManager(private val context: Context) {

    // Function to check if network is available
    @SuppressLint("ServiceCast") // Suppress lint warning for getSystemService cast
    fun isNetworkAvailable(): Boolean {
        // Get the ConnectivityManager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Get the network capabilities of the active network
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        // Check if network capabilities exist and if either cellular or WiFi transport is available
        return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        ))
    }
}
