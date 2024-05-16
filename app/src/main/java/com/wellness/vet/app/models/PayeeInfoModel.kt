package com.wellness.vet.app.models

data class PayeeInfoModel(
    val cardHolderName: String,
    val cardNumber: String,
    val cardExpiry: String,
    val cardCVV: String
)
