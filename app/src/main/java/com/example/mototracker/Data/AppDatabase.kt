package com.example.mototracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.annotation.NonNull

@Database(entities = [User::class, Motorcycle::class], version = 4) // Incrementa de 1 a 2 (o el número anterior + 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mototracker-db"
                )
                    .fallbackToDestructiveMigration() // Opcional: destruye y recrea la base de datos
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}