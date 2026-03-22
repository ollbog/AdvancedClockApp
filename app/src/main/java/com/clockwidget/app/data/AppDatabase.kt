package com.advancedclock.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Preset::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clock_widget_db"
                )
                    .fallbackToDestructiveMigration() // Safe: presets are user-optional, no critical data lost
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
