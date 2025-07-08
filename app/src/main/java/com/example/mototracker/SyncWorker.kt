package com.example.mototracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mototracker.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val firestore = FirebaseFirestore.getInstance()

        return withContext(Dispatchers.IO) {
            try {
                // Sincronizar usuarios
                val unsyncedUsers = db.appDao().getUnsyncedUsers()
                for (user in unsyncedUsers) {
                    val userData = hashMapOf(
                        "name" to user.name,
                        "email" to user.email,
                        "phone" to user.phone,
                        "password" to user.password, // Nota: Considera hashear la contraseña antes de enviar
                        "company" to user.company,
                        "synced" to true
                    )
                    try {
                        val documentRef = firestore.collection("users").add(userData).await()
                        // Marca como sincronizado solo después de éxito
                        db.appDao().markUserAsSynced(user.copy(synced = true))
                        Log.d("SYNC", "Usuario ${user.id} sincronizado con ID: ${documentRef.id}")
                    } catch (e: Exception) {
                        Log.e("SYNC", "Error sincronizando usuario ${user.id}: ${e.message}")
                        return@withContext Result.retry() // Retorna retry si falla un usuario
                    }
                }

                // Sincronizar motocicletas
                val unsyncedMotorcycles = db.appDao().getUnsyncedMotorcycles()
                for (motorcycle in unsyncedMotorcycles) {
                    val motorcycleData = hashMapOf(
                        "userId" to motorcycle.userId,
                        "brand" to motorcycle.brand,
                        "model" to motorcycle.model,
                        "plate" to motorcycle.plate,
                        "synced" to true
                    )
                    try {
                        val documentRef = firestore.collection("motorcycles").add(motorcycleData).await()
                        // Marca como sincronizado solo después de éxito
                        db.appDao().markMotorcycleAsSynced(motorcycle.copy(synced = true))
                        Log.d("SYNC", "Motocicleta ${motorcycle.plate} sincronizada con ID: ${documentRef.id}")
                    } catch (e: Exception) {
                        Log.e("SYNC", "Error sincronizando motocicleta ${motorcycle.plate}: ${e.message}")
                        return@withContext Result.retry() // Retorna retry si falla una motocicleta
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("SYNC", "Error general: ${e.message}")
                Result.retry() // Retorno general para reintentar
            }
        }
    }
}