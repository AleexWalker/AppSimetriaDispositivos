package com.example.appsimetria.requests.response

import com.example.appsimetria.requests.models.DispositivoGetAll
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseDevice (
    @SerialName("status") val status: Int,
    @SerialName("result") val result: ArrayList<DispositivoGetAll>,
)