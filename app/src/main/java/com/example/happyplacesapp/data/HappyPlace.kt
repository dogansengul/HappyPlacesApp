package com.example.happyplacesapp.data

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happyPlaceTable")
data class HappyPlace(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Int = 0,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("description") val description: String,
    @ColumnInfo("date") val date: String,
    @ColumnInfo("location") val location: String,
    @ColumnInfo("image") val imageBitmap: Bitmap
)