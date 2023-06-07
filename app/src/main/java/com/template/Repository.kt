package com.template

interface Repository {
    suspend fun getServer(
        packageId: String,
        userId: String,
        timeZone: String,
        utmParams: String
    ):ServerResponse
}