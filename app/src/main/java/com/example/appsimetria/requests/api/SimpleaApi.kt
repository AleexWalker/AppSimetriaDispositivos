package com.example.appsimetria.requests.api

import com.example.appsimetria.requests.models.Dispositivo
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.response.ResponseDevice
import com.example.appsimetria.requests.response.ResponseOperation
import retrofit2.http.*

interface SimpleaApi {

    /**
     * POST ADD DEVICE
     */
    @POST("/app/device/add")
    @FormUrlEncoded
    suspend fun postAddDevice(
        @Field("usuario") usuario: String,
        @Field("mac") mac: String,
        @Field("calle") calle: String,
        @Field("codigo_postal") codigoPostal: String,
        @Field("poblacion") localidad: String,
        @Field("fecha") fecha: String,
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("provincia") comunidad: String,
        @Field("marca") marca: String,
        @Field("modelo") modelo: String,
        @Field("numero") numero: String,
        @Field("pais") pais: String,
    ): ResponseOperation

    @POST("/app/device/add")
    suspend fun postAddDeviceJSON(
        @Body dispositivo: Dispositivo
    ): ResponseOperation

    /**
     * POST DEVICE UPDATE
     */
    @POST("/app/device/update")
    @FormUrlEncoded
    suspend fun postUpdateDevice(
        @Field("usuario") usuario: String,
        @Field("mac") mac: String,
        @Field("calle") calle: String,
        @Field("codigo_postal") codigoPostal: String,
        @Field("poblacion") localidad: String,
        @Field("fecha") fecha: String,
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("provincia") comunidad: String,
        @Field("marca") marca: String,
        @Field("modelo") modelo: String,
        @Field("numero") numero: String,
        @Field("pais") pais: String,
        @Field("dispositivo_id") dispositivo_id: String
    ): ResponseOperation

    /**
     * POST DEVICE DELETE
     */
    @POST("/app/device/delete")
    @FormUrlEncoded
    suspend fun postDeleteDevice(
        @Field("dispositivo_id") dispositivo_id: String
    ): ResponseOperation

    /**
     * GET REQUESTS
     */
    @GET("/app/devices/1")
    suspend fun getAllDevices(
    ): ResponseDevice
}