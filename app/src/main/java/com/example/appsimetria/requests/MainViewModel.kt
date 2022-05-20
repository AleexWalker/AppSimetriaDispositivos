package com.example.appsimetria.requests

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsimetria.requests.models.Dispositivo
import com.example.appsimetria.requests.repository.Repository
import com.example.appsimetria.requests.response.ResponseOperation
import com.example.appsimetria.requests.response.ResponseDevice
import kotlinx.coroutines.launch

class MainViewModel (private val repository: Repository): ViewModel() {

    var postResponseJSON: MutableLiveData<ResponseOperation> = MutableLiveData()
    var postResponse: MutableLiveData<ResponseOperation> = MutableLiveData()
    var getResponse: MutableLiveData<ResponseDevice> = MutableLiveData()

    /**
     * POST ADD DEVICE
     */
    fun postAddDevice(usuario: String, mac: String, calle: String, codigo_postal: String, poblacion: String, fecha: String, latitud: Double, longitud: Double, provincia: String, marca: String, modelo: String, numero: String, pais: String) {
        viewModelScope.launch {
            val response: ResponseOperation = repository.postAddDevice(usuario, mac, calle, codigo_postal, poblacion, fecha, latitud, longitud, provincia, marca, modelo,  numero, pais)
            postResponse.value = response
        }
    }

    fun postAddDeviceJSON(dispositivo: Dispositivo) {
        viewModelScope.launch {
            val response: ResponseOperation = repository.postAddDeviceJSON(dispositivo)
            postResponseJSON.value = response
        }
    }

    /**
     * POST UPDATE DEVICE
     */
    fun postUpdateDevice(usuario: String, mac: String, calle: String, codigo_postal: String, poblacion: String, fecha: String, latitud: Double, longitud: Double, provincia: String, marca: String, modelo: String, numero: String, pais: String, dispositivo_id: String) {
        viewModelScope.launch {
            val response: ResponseOperation = repository.postUpdateDevice(usuario, mac, calle, codigo_postal, poblacion, fecha, latitud, longitud, provincia, marca, modelo,  numero, pais, dispositivo_id)
            postResponse.value = response
        }
    }

    fun postDeleteDevice(dispositivo_id: String) {
        viewModelScope.launch {
            val response: ResponseOperation = repository.postDeleteDevice(dispositivo_id)
            postResponse.value = response
        }
    }

    fun getAllDevices() {
        viewModelScope.launch {
            val response: ResponseDevice = repository.getAllDevices()
            getResponse.value = response
        }
    }
}