package com.wellness.vet.app.models

data class UserProfileModel(
    var phoneNo: String = "",
    var name: String = "",
    var city: String = "",
    var email: String = "",
    var gender: String = "",
    var accountNumber: String = "",
    var imgUrl: String = "",
    var id: String = "",
    var fcmToken: String = ""
)