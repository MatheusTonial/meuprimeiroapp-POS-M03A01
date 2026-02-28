package com.example.meuprimeiroapp.service

import com.example.meuprimeiroapp.database.dao.UserLocationDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class GeoLocationInterceptor (private val userLocationDao: UserLocationDao) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val userLastLocation = runBlocking {
            userLocationDao.getLastLocation()
        }

        val originalRequest = chain.request()
        val newRequest = userLastLocation?.let {
            originalRequest.newBuilder()
                .addHeader("x-data-Latitude", userLastLocation.latitude.toString())
                .addHeader("x-data-Longitude", userLastLocation.longitude.toString())
                .build()
        }?: originalRequest

        return chain.proceed(newRequest)

    }
}