package com.wellness.vet.app.main_utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {

    private const val EARTH_RADIUS = 6371 // Radius of the earth in kilometers

    fun calculateDistance(userLat: Double, userLong: Double, doctorLat: Double, doctorLong: Double): Double {
        // Convert latitude and longitude from degrees to radians
        val userLatitude = Math.toRadians(userLat)
        val userLongitude = Math.toRadians(userLong)
        val doctorLatitude = Math.toRadians(doctorLat)
        val doctorLongitude = Math.toRadians(doctorLong)

        // Haversine formula
        val dLat = doctorLatitude - userLatitude
        val dLon = doctorLongitude - userLongitude
        val a = sin(dLat / 2).pow(2) + cos(userLatitude) * cos(doctorLatitude) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS * c // Distance in kilometers
    }
}
