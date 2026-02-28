package com.example.meuprimeiroapp

import android.app.Application
import com.example.meuprimeiroapp.database.DatabaseBuilder
import com.example.meuprimeiroapp.database.model.UserLocation

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        DatabaseBuilder.getInstance(this)
    }

}