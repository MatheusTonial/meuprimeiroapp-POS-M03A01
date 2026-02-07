package com.example.meuprimeiroapp.service

import com.example.meuprimeiroapp.model.Item
import com.example.meuprimeiroapp.model.ItemValue
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ItemApiService {

    @GET("items")
    suspend fun getItems(): List<Item>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    @PATCH("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ItemValue) : Item

    @POST("items")
    suspend fun addItem(@Body item: ItemValue) : Item
}



