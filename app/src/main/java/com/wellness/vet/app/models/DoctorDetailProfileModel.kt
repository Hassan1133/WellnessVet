package com.wellness.vet.app.models

class DoctorDetailProfileModel(
    var phoneNo: String = "",
    var name: String = "",
    var id: String = "",
    var email: String = "",
    var city: String = "",
    var gender: String = "",
    var accountNumber: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var fees: String = "",
    var imgUrl: String = "",
    var clinicLocation: String = "",
    var clinicLatitude: Double = 0.0,
    var clinicLongitude: Double = 0.0,
    var fcmToken : String = ""
)