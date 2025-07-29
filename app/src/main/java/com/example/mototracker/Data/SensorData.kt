// com.example.mototracker.data/SensorData.kt
package com.example.mototracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val lat: Double,
    val lng: Double,
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
    val gyroX: Double,
    val gyroY: Double,
    val gyroZ: Double,
    val speed: Double,
    @ColumnInfo(name = "synced") var synced: Boolean = false
)

@Entity(tableName = "calculated_speed")
data class SpeedData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val calculatedSpeed: Double
)

@Entity(tableName = "accident_events")
data class AccidentEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val accelMagnitude: Double,
    val gyroMagnitude: Double,
    val lat: Double,
    val lng: Double
)