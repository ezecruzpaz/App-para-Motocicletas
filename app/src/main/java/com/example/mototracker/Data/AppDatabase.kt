package com.example.mototracker.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.annotation.NonNull
import com.example.mototracker.BluetoothService


@Database(
    entities = [
        User::class,
        Motorcycle::class,
        EmergencyContact::class,
        SensorData::class,
        SpeedData::class,
        AccidentEvent::class
    ],
    version = 9 // Aumenta la versión para forzar una recreación
)

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