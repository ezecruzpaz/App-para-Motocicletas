package com.example.mototracker.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Cambiado a REPLACE para evitar conflictos
    suspend fun insertMotorcycle(motorcycle: Motorcycle)

    @Query("SELECT * FROM motorcycles WHERE userId = :userId")
    suspend fun getMotorcyclesByUserId(userId: Long): List<Motorcycle>

    @Update
    suspend fun updateMotorcycle(motorcycle: Motorcycle) // Nuevo método para actualizar

    @Update
    suspend fun markUserAsSynced(user: User)

    @Update
    suspend fun markMotorcycleAsSynced(motorcycle: Motorcycle)

    @Query("SELECT * FROM users WHERE synced = 0")
    suspend fun getUnsyncedUsers(): List<User>

    @Query("SELECT * FROM motorcycles WHERE synced = 0")
    suspend fun getUnsyncedMotorcycles(): List<Motorcycle>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithMotorcycles(userId: Long): UserWithMotorcycles?

    data class UserWithMotorcycles(
        @Embedded val user: User,
        @Relation(
            parentColumn = "id",
            entityColumn = "userId"
        )
        val motorcycles: List<Motorcycle>
    )
}