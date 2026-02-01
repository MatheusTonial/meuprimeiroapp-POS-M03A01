package com.example.meuprimeiroapp.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.3.100:3000/" // Endereço usado para acessar o localhost no emulador Android

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }
}