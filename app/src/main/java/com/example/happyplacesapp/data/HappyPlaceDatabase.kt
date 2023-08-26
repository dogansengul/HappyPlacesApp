package com.example.happyplacesapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HappyPlace::class], version = 2)
@TypeConverters(Converters::class)
abstract class HappyPlaceDatabase: RoomDatabase() {
    abstract fun dao(): Dao

    companion object {
        @Volatile
        private var INSTANCE: HappyPlaceDatabase? = null

        fun getDatabase(context: Context): HappyPlaceDatabase? {
            var instance = INSTANCE
            if (INSTANCE != null) {
                return instance
            } else {
                synchronized(this) {
                    val newInstance = Room.databaseBuilder(
                        context.applicationContext,
                        HappyPlaceDatabase::class.java,
                        "happyplace-database").fallbackToDestructiveMigration().build()
                    instance = newInstance
                }
                return instance
            }
        }
    }
}