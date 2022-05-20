package com.example.appsimetria.requests.models

import com.google.gson.annotations.SerializedName

class DispositivoGetAll (
    @SerializedName("dispositivo_id") val dispositivo: Int,
    @SerializedName("mac") val mac: String,
    @SerializedName("calle") val calle: String,
    @SerializedName("codigo_postal") val codigo_postal: String,
    @SerializedName("poblacion_id") val poblacion_id: Int,
    @SerializedName("poblacion_nombre") val poblacion_nombre: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("lat") val latitud: String,
    @SerializedName("lon") val longitud: String,
    @SerializedName("provincia_id") val provincia_id: Int,
    @SerializedName("provincia_nombre") val provincia_nombre: String,
    @SerializedName("marca") val marca: String,
    @SerializedName("modelo")val modelo: String,
    @SerializedName("numero") val numero: String,
    @SerializedName("pais_id") val pais_id: Int,
    @SerializedName("nombre") val nombre: String,
){
}