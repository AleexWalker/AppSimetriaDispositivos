package com.example.appsimetria.requests.api

import com.example.appsimetria.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://gesmach.essmartweb.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: SimpleaApi by lazy {
        retrofit.create(SimpleaApi::class.java)
    }
}