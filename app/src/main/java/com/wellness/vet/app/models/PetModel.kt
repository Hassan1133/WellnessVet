package com.wellness.vet.app.models

import java.io.Serializable

data class PetModel(
    var id: String = "",
    var name: String = "",
    var gender: String = "",
    var age: String = "",
    var breed: String = ""
) : Serializable
