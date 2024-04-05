package com.wellness.vet.app.models

class DoctorDetailProfileModel(
    var phoneNo: String = "",
    var name: String = "",
    var city: String = "",
    var gender: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var fees: String = "",
    var clinicLocation: String = "",
    var clinicLatitude: Double = 0.0,
    var clinicLongitude: Double = 0.0,
)