package com.wellness.vet.app.payments.model

import com.google.gson.annotations.SerializedName

class FetchTokenModel {
    @SerializedName("consented_on")
    var consented_on = 0

    @SerializedName("expires_in")
    var expires_in = 0

    @SerializedName("scope")
    var scope: String? = null

    @SerializedName("access_token")
    var access_token: String? = null

    @SerializedName("token_type")
    var token_type: String? = null

    constructor()
    constructor(
        consented_on: Int,
        expires_in: Int,
        scope: String?,
        access_token: String?,
        token_type: String?
    ) {
        this.consented_on = consented_on
        this.expires_in = expires_in
        this.scope = scope
        this.access_token = access_token
        this.token_type = token_type
    }
}
