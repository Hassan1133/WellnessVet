package com.wellness.vet.app.models

import java.io.Serializable

data class UserAppointmentListModel(
    var uId: String = "",
    var name: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var clinicLocation: String = "",
    var clinicLatitude: Float = 0.0f,
    var clinicLongitude: Float = 0.0f,
    var key: String = "",
    var date: String = "",
    var time: String = ""
) : Serializable