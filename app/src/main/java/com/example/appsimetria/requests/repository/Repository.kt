package com.example.appsimetria.requests.repository

import com.example.appsimetria.requests.api.RetrofitInstance
import com.example.appsimetria.requests.models.Dispositivo
import com.example.appsimetria.requests.response.ResponseOperation
import com.example.appsimetria.requests.response.ResponseDevice

class Repository {

    /**
     * POST FUN ADD DEVICE
     */
    suspend fun postAddDevice(usuario: String, mac: String, calle: String, codigo_postal: String, poblacion: String, fecha: String, latitud: Double, longitud: Double, provincia: String, marca: String, modelo: String, numero: String, pais: String): ResponseOperation {
        return RetrofitInstance.api.postAddDevice(usuario, mac, calle, codigo_postal, poblacion, fecha, latitud, longitud, provincia, marca, modelo,  numero, pais)
    }

    suspend fun postAddDeviceJSON(dispositivo: Dispositivo): ResponseOperation {
        return RetrofitInstance.api.postAddDeviceJSON(dispositivo)
    }

    /**
     * POST FUN UPDATE DEVICE
     */
    suspend fun postUpdateDevice(usuario: String, mac: String, calle: String, codigo_postal: String, poblacion: String, fecha: String, latitud: Double, longitud: Double, provincia: String, marca: String, modelo: String, numero: String, pais: String, dispositivo_id:String): ResponseOperation {
        return RetrofitInstance.api.postUpdateDevice(usuario, mac, calle, codigo_postal, poblacion, fecha, latitud, longitud, provincia, marca, modelo,  numero, pais, dispositivo_id)
    }

    /**
     * POST FUN DELETE DEVICE
     */
    suspend fun postDeleteDevice(dispositivo_id: String): ResponseOperation {
        return RetrofitInstance.api.postDeleteDevice(dispositivo_id)
    }

    /**
     * GET FUN
     */
    suspend fun getAllDevices(): ResponseDevice {
        return RetrofitInstance.api.getAllDevices()
    }
}