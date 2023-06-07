package com.template

class RepositoryImpl(
    private val api: ApplicationApi
):Repository {
    override suspend fun getServer(
        packageId: String,
        userId: String,
        timeZone: String,
        utmParams: String
    ): ServerResponse = api.getData(packageId,userId,timeZone,utmParams)
}