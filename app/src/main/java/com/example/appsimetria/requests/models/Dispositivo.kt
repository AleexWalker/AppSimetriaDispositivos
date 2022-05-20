package com.example.appsimetria.requests.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

data class Dispositivo (
    @SerializedName("calle") val calle: String,
    @SerializedName("codigo_postal") val codigo_postal: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("marca") val marca: String,
    @SerializedName("modelo")val modelo: String,
    @SerializedName("mac") val mac: String,
    @SerializedName("numero") val numero: String,
    @SerializedName("poblacion") val poblacion: String,
    @SerializedName("provincia") val provincia: String,
    @SerializedName("pais") val pais: String,
    @SerializedName("usuario") val usuario: String,
)

/**
data class Dispositivo (
    @SerialName("ID") val ID: String,
    @SerialName("mac") val mac: String,
    @SerialName("latitud") val latitud: Double,
    @SerialName("longitud") val longitud: Double,
    @SerialName("fecha") val fecha: String,
    @SerialName("hora") val hora: String,
    @SerialName("poblacion") val localidad: String,
    @SerialName("numero") val numero: String,
    @SerialName("ciudad") val ciudad: String,
    @SerialName("provincia") val comunidad: String,
    @SerialName("pais") val pais: String,
    @SerialName("calle") val calle: String,
    @SerialName("codigo_postal") val codigoPostal: String,
    @SerialName("marca") val marca: String,
    @SerialName("modelo") val modelo: String,
)
 */