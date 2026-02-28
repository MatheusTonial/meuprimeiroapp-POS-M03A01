package com.example.meuprimeiroapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meuprimeiroapp.database.model.UserLocation

@Dao
interface UserLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLocation: UserLocation)

    @Query("SELECT * FROM user_location_table")
    suspend fun getAllLocations(): List<UserLocation>

    @Query("SELECT * FROM user_location_table order by id desc ")
    suspend fun getLastLocation(): UserLocation?


}