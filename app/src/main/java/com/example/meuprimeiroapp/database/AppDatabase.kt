package com.example.meuprimeiroapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.meuprimeiroapp.database.converters.DateConverters
import com.example.meuprimeiroapp.database.dao.UserLocationDao
import com.example.meuprimeiroapp.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 1, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userLocationDao(): UserLocationDao


}