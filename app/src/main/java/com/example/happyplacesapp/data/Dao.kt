package com.example.happyplacesapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlace(place: HappyPlace)

    @Delete
    suspend fun deletePlace(place: HappyPlace)

    @Query("SELECT * FROM 'happyPlaceTable' ORDER BY 'id' ASC")
    fun getAllPlaces(): Flow<List<HappyPlace>>

    @Query("SELECT COUNT(*) FROM happyPlaceTable")
    suspend fun getPlaceCount(): Int
}