package com.example.mototracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val company: String,
    val imageUri: String? = null, // Nuevo campo para la URI de la imagen
    var synced: Boolean = false
)