package com.template

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApplicationApi {
    @GET("/")
    fun getData(
        @Query("packageid") packageId: String,
        @Query("usserid") userId: String,
        @Query("getz") timeZone: String,
        @Query("getr") utmParams: String
    ): Call<String>
}