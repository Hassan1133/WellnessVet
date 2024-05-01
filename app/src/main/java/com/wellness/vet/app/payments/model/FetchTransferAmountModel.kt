package com.wellness.vet.app.payments.model

import com.google.gson.annotations.SerializedName

class FetchTransferAmountModel {
    @SerializedName("ResponseDetail")
    var responseDetail: String? = null

    @SerializedName("Reserved3")
    var reserved3: String? = null

    @SerializedName("Reserved2")
    var reserved2: String? = null

    @SerializedName("Reserved1")
    var reserved1: String? = null

    @SerializedName("AccountNumberTo")
    var accountNumberTo: String? = null

    @SerializedName("ToBankIMD")
    var toBankIMD: String? = null

    @SerializedName("AuthorizationIdentificationResponse")
    var authorizationIdentificationResponse: String? = null

    @SerializedName("RRN")
    var rRN: String? = null

    @SerializedName("DateSettlement")
    var dateSettlement: String? = null

    @SerializedName("DateLocalTransaction")
    var dateLocalTransaction: String? = null

    @SerializedName("TimeLocalTransaction")
    var timeLocalTransaction: String? = null

    @SerializedName("STAN")
    var sTAN: String? = null

    @SerializedName("TransmissionDateAndTime")
    var transmissionDateAndTime: String? = null

    @SerializedName("Amount")
    var amount: String? = null

    @SerializedName("PAN")
    var pAN: String? = null

    @SerializedName("ResponseCode")
    var responseCode: String? = null

    constructor()
    constructor(
        responseDetail: String?,
        reserved3: String?,
        reserved2: String?,
        reserved1: String?,
        accountNumberTo: String?,
        toBankIMD: String?,
        authorizationIdentificationResponse: String?,
        RRN: String?,
        dateSettlement: String?,
        dateLocalTransaction: String?,
        timeLocalTransaction: String?,
        STAN: String?,
        transmissionDateAndTime: String?,
        amount: String?,
        PAN: String?,
        responseCode: String?
    ) {
        this.responseDetail = responseDetail
        this.reserved3 = reserved3
        this.reserved2 = reserved2
        this.reserved1 = reserved1
        this.accountNumberTo = accountNumberTo
        this.toBankIMD = toBankIMD
        this.authorizationIdentificationResponse = authorizationIdentificationResponse
        rRN = RRN
        this.dateSettlement = dateSettlement
        this.dateLocalTransaction = dateLocalTransaction
        this.timeLocalTransaction = timeLocalTransaction
        sTAN = STAN
        this.transmissionDateAndTime = transmissionDateAndTime
        this.amount = amount
        pAN = PAN
        this.responseCode = responseCode
    }

    override fun toString(): String {
        return "FetchTransferAmountModel{" +
                "ResponseDetail='" + responseDetail + '\'' +
                ", Reserved3='" + reserved3 + '\'' +
                ", Reserved2='" + reserved2 + '\'' +
                ", Reserved1='" + reserved1 + '\'' +
                ", AccountNumberTo='" + accountNumberTo + '\'' +
                ", ToBankIMD='" + toBankIMD + '\'' +
                ", AuthorizationIdentificationResponse='" + authorizationIdentificationResponse + '\'' +
                ", RRN='" + rRN + '\'' +
                ", DateSettlement='" + dateSettlement + '\'' +
                ", DateLocalTransaction='" + dateLocalTransaction + '\'' +
                ", TimeLocalTransaction='" + timeLocalTransaction + '\'' +
                ", STAN='" + sTAN + '\'' +
                ", TransmissionDateAndTime='" + transmissionDateAndTime + '\'' +
                ", Amount='" + amount + '\'' +
                ", PAN='" + pAN + '\'' +
                ", ResponseCode='" + responseCode + '\'' +
                '}'
    }
}
