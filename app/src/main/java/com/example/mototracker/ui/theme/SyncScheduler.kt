package com.example.mototracker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun scheduleSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 12) // 12:00 PM CST
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // Si ya pasó las 12:00 PM, no sumamos un día porque queremos que sea hoy
            if (timeInMillis <= currentTime) {
                // No añadimos días, ya que 12:00 PM está en el futuro (dentro de 10 minutos)
            }
        }

        val initialDelay = calendar.timeInMillis - currentTime
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "syncWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
        Log.d("SyncScheduler", "Sincronización programada para las 12:00 PM CST de hoy")
    }
}