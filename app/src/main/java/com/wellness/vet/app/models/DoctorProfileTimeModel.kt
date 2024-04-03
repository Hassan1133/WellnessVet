package com.wellness.vet.app.models

data class DoctorProfileTimeModel(
    var startTime: String = "",
    var endTime: String = "",
    var fees: String = "",
    var clinicLocation: String = "",
    var clinicLatitude: Double = 0.0,
    var clinicLongitude: Double = 0.0,
)
