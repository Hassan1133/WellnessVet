package com.wellness.vet.app.models

import java.io.Serializable

class DoctorAppointmentListModel(
    var uId: String = "",
    var name: String = "",
    var date: String = "",
    var appointmentStatus: String = "",
    var time: String = ""
) : Serializable