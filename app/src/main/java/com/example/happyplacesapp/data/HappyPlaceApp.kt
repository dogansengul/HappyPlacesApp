package com.example.happyplacesapp.data

import android.app.Application

class HappyPlaceApp: Application() {
    val db by lazy {
        HappyPlaceDatabase.getDatabase(this)
    }
}