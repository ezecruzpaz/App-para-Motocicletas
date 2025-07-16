package com.example.mototracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val fullName: String,
    val phoneNumber: String,
    val relationship: String? = null,
    val email: String? = null
)