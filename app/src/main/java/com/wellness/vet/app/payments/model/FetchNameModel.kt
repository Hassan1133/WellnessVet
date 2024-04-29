package com.wellness.vet.app.payments.model

data class FetchNameModel(
    val Alias: String,
    val Amount: String,
    val AuthorizationIdentificationResponse: String,
    val BankName: String,
    val DateLocalTransaction: String,
    val DateSettlement: String,
    val IBAN_MobileAccountNumber: String,
    val Pan: String,
    val Reserved1: String,
    val Reserved2: String,
    val Reserved3: String,
    val ResponseCode: String,
    val ResponseDetail: String,
    val RetrievalReferenceNumber: String,
    val Stan: String,
    val TimeLocalTransaction: String,
    val ToBankImd: String,
    val TransmissionDateAndTime: String,
    val accountNumberTo: String,
    val accountTitleTo: String,
    val branchNameTo: String
)