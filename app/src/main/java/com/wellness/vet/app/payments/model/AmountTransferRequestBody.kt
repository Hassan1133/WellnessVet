package com.wellness.vet.app.payments.model

import com.google.gson.annotations.SerializedName

class AmountTransferRequestBody {
    @SerializedName("SenderIBAN")
    var senderIBAN: String? = null

    @SerializedName("SenderName")
    var senderName: String? = null

    @SerializedName("ReferenceNo")
    var referenceNo: String? = null

    @SerializedName("RTPID")
    var rTPID: String? = null

    @SerializedName("Reserved3")
    var reserved3: String? = null

    @SerializedName("Reserved2")
    var reserved2: String? = null

    @SerializedName("Reserved1")
    var reserved1: String? = null

    @SerializedName("BeneficiaryBankName")
    var beneficiaryBankName: String? = null

    @SerializedName("PosEntryMode")
    var posEntryMode: String? = null

    @SerializedName("ExpiryDate")
    var expiryDate: String? = null

    @SerializedName("PAN")
    var pAN: String? = null

    @SerializedName("ToBankIMD")
    var toBankIMD: String? = null

    @SerializedName("AccountNumberTo")
    var accountNumberTo: String? = null

    @SerializedName("AccountNumberFrom")
    var accountNumberFrom: String? = null

    @SerializedName("CurrencyCodeTransaction")
    var currencyCodeTransaction: String? = null

    @SerializedName("PaymentDetail")
    var paymentDetail: String? = null

    @SerializedName("MerchantDetail")
    var merchantDetail: String? = null

    @SerializedName("CardAcceptorNameLocation")
    var cardAcceptorNameLocation: CardAcceptorNameLocation? = null

    @SerializedName("CardAcceptorIdCode")
    var cardAcceptorIdCode: String? = null

    @SerializedName("CardAcceptorTerminalId")
    var cardAcceptorTerminalId: String? = null

    @SerializedName("AuthorizationIdentificationResponse")
    var authorizationIdentificationResponse: String? = null

    @SerializedName("RRN")
    var rRN: String? = null

    @SerializedName("FromBankIMD")
    var fromBankIMD: String? = null

    @SerializedName("MerchantType")
    var merchantType: String? = null

    @SerializedName("Date")
    var date: String? = null

    @SerializedName("Time")
    var time: String? = null

    @SerializedName("STAN")
    var sTAN: String? = null

    @SerializedName("TransmissionDateAndTime")
    var transmissionDateAndTime: String? = null

    @SerializedName("TransactionAmount")
    var transactionAmount: String? = null

    class CardAcceptorNameLocation {
        @SerializedName("Country")
        var country: String? = null

        @SerializedName("BankName")
        var bankName: String? = null

        @SerializedName("AgentCity")
        var agentCity: String? = null

        @SerializedName("ADCLiteral")
        var aDCLiteral: String? = null

        @SerializedName("AgentName")
        var agentName: String? = null

        @SerializedName("ZipCode")
        var zipCode: String? = null

        @SerializedName("State")
        var state: String? = null

        @SerializedName("City")
        var city: String? = null

        @SerializedName("Location")
        var location: String? = null

        constructor()
        constructor(
            location: String,
            city: String,
            state: String,
            zipCode: String,
            agentName: String,
            ADCLiteral: String,
            agentCity: String,
            bankName: String,
            country: String
        ) {
            this.country = country
            this.bankName = bankName
            this.agentCity = agentCity
            aDCLiteral = ADCLiteral
            this.agentName = agentName
            this.zipCode = zipCode
            this.state = state
            this.city = city
            this.location = location
        }
    }

    constructor()
    constructor(
        transactionAmount: String?,
        transmissionDateAndTime: String?,
        STAN: String?,
        time: String?,
        date: String?,
        merchantType: String?,
        fromBankIMD: String?,
        RRN: String?,
        authorizationIdentificationResponse: String?,
        cardAcceptorTerminalId: String?,
        cardAcceptorIdCode: String?,
        cardAcceptorNameLocation: CardAcceptorNameLocation?,
        merchantDetail: String?,
        paymentDetail: String?,
        currencyCodeTransaction: String?,
        accountNumberFrom: String?,
        accountNumberTo: String?,
        toBankIMD: String?,
        PAN: String?,
        expiryDate: String?,
        posEntryMode: String?,
        beneficiaryBankName: String?,
        reserved1: String?,
        reserved2: String?,
        reserved3: String?,
        RTPID: String?,
        referenceNo: String?,
        senderName: String?,
        senderIBAN: String?
    ) {
        this.senderIBAN = senderIBAN
        this.senderName = senderName
        this.referenceNo = referenceNo
        rTPID = RTPID
        this.reserved3 = reserved3
        this.reserved2 = reserved2
        this.reserved1 = reserved1
        this.beneficiaryBankName = beneficiaryBankName
        this.posEntryMode = posEntryMode
        this.expiryDate = expiryDate
        pAN = PAN
        this.toBankIMD = toBankIMD
        this.accountNumberTo = accountNumberTo
        this.accountNumberFrom = accountNumberFrom
        this.currencyCodeTransaction = currencyCodeTransaction
        this.paymentDetail = paymentDetail
        this.merchantDetail = merchantDetail
        this.cardAcceptorNameLocation = cardAcceptorNameLocation
        this.cardAcceptorIdCode = cardAcceptorIdCode
        this.cardAcceptorTerminalId = cardAcceptorTerminalId
        this.authorizationIdentificationResponse = authorizationIdentificationResponse
        rRN = RRN
        this.fromBankIMD = fromBankIMD
        this.merchantType = merchantType
        this.date = date
        this.time = time
        sTAN = STAN
        this.transmissionDateAndTime = transmissionDateAndTime
        this.transactionAmount = transactionAmount
    }


}
