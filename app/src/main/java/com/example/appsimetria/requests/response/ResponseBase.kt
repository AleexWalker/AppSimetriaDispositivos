package com.example.appsimetria.requests.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseBase<T>(
    val status: Int,
    val title: T,
    val message: String,
){
}