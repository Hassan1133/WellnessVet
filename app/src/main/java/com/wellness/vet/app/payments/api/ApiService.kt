package com.wellness.vet.app.payments.api

import com.wellness.vet.app.payments.model.AmountTransferRequestBody
import com.wellness.vet.app.payments.model.FetchTokenModel
import com.wellness.vet.app.payments.model.FetchTransferAmountModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("uat-1link/sandbox/oauth2/token")
    fun getAccessToken(
        @Field("grant_type") grant_type: String,
        @Field("scope") scope: String,
        @Field("client_id") client_id: String,
        @Field("client_secret") client_secret: String
    ): Call<FetchTokenModel>


    @POST("uat-1link/sandbox/funds-transfer-rest-service/path-1")
    fun transferAmount(
        @Header("Authorization") auth: String,
        @Header("X-IBM-Client-Id") client_id: String,
        @Body body: AmountTransferRequestBody
    ): Call<FetchTransferAmountModel>

}
