package com.template

data class ServerResponseState(
    var isLoading: Boolean = false,
    var response: ServerResponse? = null,
    val error: String = ""
)
