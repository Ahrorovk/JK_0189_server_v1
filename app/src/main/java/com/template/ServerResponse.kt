package com.template

data class ServerResponse(
    val code: Int,
    val message: String?,
    val protocol: String?,
    val url: String?
)

