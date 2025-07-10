package com.example.mototracker

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothService : Service() {

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var serverSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(1, notification)

        scope.launch {
            startBluetoothServer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startBluetoothServer() {
        Log.d("BluetoothService", "Iniciando servidor Bluetooth...")
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            Log.w("BluetoothService", "Bluetooth desactivado")
            stopSelf()
            return
        }

        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTServer", MY_UUID)
            Log.d("BluetoothService", "Esperando conexión...")

            while (true) {
                val socket = serverSocket?.accept() ?: continue
                bluetoothSocket = socket
                Log.d("BluetoothService", "Conexión establecida")
                // Iniciar la lectura en una nueva corrutina
                scope.launch {
                    readInputStream(socket.inputStream)
                }
            }
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error: ${e.message}")
        } finally {
            serverSocket?.close()
            bluetoothSocket?.close()
            stopSelf()
        }
    }

    private suspend fun readInputStream(inputStream: java.io.InputStream) {
        val buffer = ByteArray(1024)
        var bytes: Int
        try {
            while (true) {
                bytes = withContext(Dispatchers.IO) { inputStream.read(buffer) }
                if (bytes > 0) {
                    val receivedText = String(buffer, 0, bytes)
                    Log.d("BluetoothService", "Mensaje recibido: $receivedText")
                    withContext(Dispatchers.Main) {
                        // Aquí puedes guardar datos o enviar confirmaciones si lo deseas
                    }
                    bluetoothSocket?.outputStream?.write("Confirmado".toByteArray())
                }
            }
        } catch (e: IOException) {
            Log.w("BluetoothService", "Cliente desconectado: ${e.message}")
        } finally {
            inputStream.close()
            bluetoothSocket?.close()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BluetoothServiceChannel",
                "Bluetooth Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, "BluetoothServiceChannel")
            .setContentTitle("Servicio Bluetooth")
            .setContentText("Ejecutando en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usa @mipmap/ic_launcher si tienes uno
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serverSocket?.close()
        bluetoothSocket?.close()
        Log.d("BluetoothService", "Servicio detenido")
    }
}