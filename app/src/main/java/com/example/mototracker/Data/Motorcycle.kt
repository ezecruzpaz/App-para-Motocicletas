package com.example.mototracker.data

import androidx.room.Entity

@Entity(tableName = "motorcycles", primaryKeys = ["userId", "plate"])
data class Motorcycle(
    val userId: Long,
    val brand: String,
    val model: String,
    val plate: String,
    var synced: Boolean = false
)