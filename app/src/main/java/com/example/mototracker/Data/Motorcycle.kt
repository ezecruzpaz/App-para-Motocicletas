package com.example.mototracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "motorcycles") // Especificar el nombre de la tabla como "motorcycles"
data class Motorcycle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val brand: String,
    val model: String,
    val year: Int?, // Opcional
    val plate: String,
    val displacement: Int?, // Opcional
    val insurance: String?, // Opcional
    var synced: Boolean = false,
    @ColumnInfo(name = "color") val color: String? = null,
    )