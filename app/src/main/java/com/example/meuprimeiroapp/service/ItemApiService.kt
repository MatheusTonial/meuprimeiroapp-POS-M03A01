package com.example.meuprimeiroapp.service

import com.example.meuprimeiroapp.model.Item
import retrofit2.http.GET

interface ItemApiService {

    @GET("items")
    suspend fun getItems(): List<Item>

}