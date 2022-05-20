package com.example.appsimetria.requests.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseOperation(
    val status: Int,
    val title: String,
    val message: String,
    val result: Int,
){
}