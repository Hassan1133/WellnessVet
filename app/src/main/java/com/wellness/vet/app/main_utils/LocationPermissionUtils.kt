package com.wellness.vet.app.main_utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.wellness.vet.app.main_utils.AppConstants.Companion.ERROR_DIALOG_REQUEST
import com.wellness.vet.app.main_utils.AppConstants.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.wellness.vet.app.main_utils.AppConstants.Companion.PERMISSIONS_REQUEST_ENABLE_GPS


class LocationPermissionUtils(private val activity: Activity) {
    var locationPermission = false

    fun checkAndRequestPermissions() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                getLocationPermission()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                getLocationPermission()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun isMapsEnabled(): Boolean {
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            false
        } else {
            true
        }
    }

    fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission = true
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun isServicesOK(): Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
        return if (available == ConnectionResult.SUCCESS) {
            true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
            false
        } else {
            Toast.makeText(activity, "You can't make map requests", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun isLocationPermissionGranted(): Boolean {
        getLocationPermission()
        return locationPermission
    }
}