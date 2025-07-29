package com.example.mototracker

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Database
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.example.mototracker.data.AppDao
import com.example.mototracker.data.EmergencyContact as DataEmergencyContact
import com.example.mototracker.data.Motorcycle as DataMotorcycle
import com.example.mototracker.data.User as DataUser
import com.example.mototracker.data.SensorData
import com.example.mototracker.data.SpeedData
import com.example.mototracker.data.AccidentEvent
import com.example.mototracker.data.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import java.io.IOException
import java.util.UUID

class BluetoothService : Service() {

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var serverSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    private val db by lazy { AppDatabase.getInstance(this) }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(1, notification)
        scope.launch { startBluetoothServer() }
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

        scope.launch {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTServer", MY_UUID)
                Log.d("BluetoothService", "Esperando conexión...")
                while (isActive) {
                    val socket = serverSocket?.accept() ?: continue
                    bluetoothSocket = socket
                    Log.d("BluetoothService", "Conexión establecida")
                    scope.launch { readInputStream(socket.inputStream) }
                }
            } catch (e: IOException) {
                Log.e("BluetoothService", "Error: ${e.message}")
            } finally {
                serverSocket?.close()
                bluetoothSocket?.close()
                stopSelf()
            }
        }
    }

    private suspend fun readInputStream(inputStream: java.io.InputStream) {
        val buffer = ByteArray(1024)
        try {
            while (isActive) {
                val bytes = withContext(Dispatchers.IO) { inputStream.read(buffer) }
                if (bytes > 0) {
                    val receivedText = String(buffer, 0, bytes).trim()
                    if (receivedText.isNotEmpty()) {
                        Log.d("BluetoothService", "Mensaje recibido: $receivedText")
                        saveToSQLite(receivedText)
                        bluetoothSocket?.outputStream?.write("Confirmado".toByteArray())
                    }
                }
            }
        } catch (e: IOException) {
            Log.w("BluetoothService", "Cliente desconectado: ${e.message}")
        } finally {
            inputStream.close()
            bluetoothSocket?.close()
        }
    }

    private fun saveToSQLite(json: String) {
        scope.launch {
            try {
                val parsed = gson.fromJson(json, ReceivedSensorData::class.java) ?: return@launch
                val accelMag = Math.sqrt(
                    parsed.sensors.accel.x * parsed.sensors.accel.x +
                            parsed.sensors.accel.y * parsed.sensors.accel.y +
                            parsed.sensors.accel.z * parsed.sensors.accel.z
                )
                val gyroMag = Math.sqrt(
                    parsed.sensors.gyro.x * parsed.sensors.gyro.x +
                            parsed.sensors.gyro.y * parsed.sensors.gyro.y +
                            parsed.sensors.gyro.z * parsed.sensors.gyro.z
                )
                val timestampStr = parsed.timestamp.toString()

                val sensor = SensorData(
                    timestamp = timestampStr,
                    lat = parsed.location.lat,
                    lng = parsed.location.lng,
                    accelX = parsed.sensors.accel.x,
                    accelY = parsed.sensors.accel.y,
                    accelZ = parsed.sensors.accel.z,
                    gyroX = parsed.sensors.gyro.x,
                    gyroY = parsed.sensors.gyro.y,
                    gyroZ = parsed.sensors.gyro.z,
                    speed = parsed.sensors.speed,
                    synced = false
                )

                db.appDao().insert(sensor) // Usar AppDao en lugar de sensorDataDao
                db.appDao().insertSpeed(SpeedData(timestamp = timestampStr, calculatedSpeed = accelMag))

                if (accelMag > 15.0 || gyroMag > 20.0) {
                    db.appDao().insertAccident(
                        AccidentEvent(
                            timestamp = timestampStr,
                            accelMagnitude = accelMag,
                            gyroMagnitude = gyroMag,
                            lat = parsed.location.lat,
                            lng = parsed.location.lng
                        )
                    )
                    Log.w("ACCIDENT", "🚨 Posible accidente detectado")
                    createEmergencyData(sensor, accelMag, gyroMag)
                }

                uploadToFirestore(sensor)
            } catch (e: Exception) {
                Log.e("SQLITE", "Error al guardar: ${e.message}")
            }
        }
    }

    private fun uploadToFirestore(sensorData: SensorData) {
        firestore.collection("sensor_data")
            .document(sensorData.timestamp)
            .set(sensorData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Datos subidos a Firestore con timestamp: ${sensorData.timestamp}")
                scope.launch {
                    val dataList = db.appDao().getUnsyncedData() // Usar AppDao
                    val foundData = dataList.firstOrNull { it.timestamp == sensorData.timestamp }
                    foundData?.let { data ->
                        data.synced = true
                        db.appDao().update(data) // Usar AppDao
                        Log.d("SQLITE", "Datos marcados como sincronizados: ${data.id}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al subir a Firestore: ${e.message}")
            }
    }

    private fun createEmergencyData(sensorData: SensorData, accelMag: Double, gyroMag: Double) {
        scope.launch {
            try {
                // Obtener userId desde SharedPreferences
                val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val userId = sharedPref.getLong("logged_in_user_id", 0L)
                if (userId == 0L) {
                    Log.e("EMERGENCY", "No se encontró userId en SharedPreferences")
                    return@launch
                }
                Log.d("EMERGENCY", "Buscando datos para userId local: $userId")

                // Obtener usuario desde la tabla local usando el userId de SharedPreferences
                val user = db.appDao().getUserById(userId) ?: run {
                    Log.e("EMERGENCY", "Usuario no encontrado para userId: $userId")
                    DataUser(0, "Desconocido", "desconocido@email.com", "5512345678", "Sin empresa", "null")
                }

                // Obtener contacto de emergencia
                val emergencyContact = db.appDao().getEmergencyContactsByUserId(userId).firstOrNull() ?: run {
                    Log.e("EMERGENCY", "Contacto de emergencia no encontrado para userId: $userId")
                    DataEmergencyContact(0, userId, "Contacto Desconocido", "Sin número", null, null)
                }

                // Obtener motocicleta
                val motorcycle = db.appDao().getMotorcyclesByUserId(userId).firstOrNull() ?: run {
                    Log.e("EMERGENCY", "Motocicleta no encontrada para userId: $userId")
                    DataMotorcycle(0, userId, "Desconocido", "Desconocido", null, "Sin placa", null, null)
                }

                // Crear el JSON de emergencia con datos locales
                val emergencyData = EmergencyData(
                    eventType = if (accelMag > 15.0 || gyroMag > 20.0) "fall" else "impact",
                    timestamp = sensorData.timestamp.toLongOrNull() ?: System.currentTimeMillis(),
                    location = LocationData(sensorData.lat, sensorData.lng),
                    sensors = SensorsData(
                        accel = AxisData(sensorData.accelX, sensorData.accelY, sensorData.accelZ),
                        gyro = AxisData(sensorData.gyroX, sensorData.gyroY, sensorData.gyroZ),
                        speed = sensorData.speed
                    ),
                    driver = UserData(
                        name = user.name,
                        phone = user.phone,
                        emergencyContact = DataEmergencyContact(
                            id = emergencyContact.id,
                            userId = emergencyContact.userId,
                            fullName = emergencyContact.fullName,
                            phoneNumber = emergencyContact.phoneNumber,
                            relationship = emergencyContact.relationship ?: "",
                            email = emergencyContact.email ?: ""
                        )
                    ),
                    motorcycle = MotorcycleData(
                        brand = motorcycle.brand,
                        model = motorcycle.model,
                        plate = motorcycle.plate,
                        color = motorcycle.color ?: "Sin color"
                    ),
                    route = RouteData(
                        startTimestamp = sensorData.timestamp.toLongOrNull() ?: System.currentTimeMillis(),
                        durationSoFar = 3600
                    )
                )

                // Subir a Firestore
                firestore.collection("emergency_data")
                    .document(emergencyData.timestamp.toString())
                    .set(emergencyData)
                    .addOnSuccessListener {
                        Log.d("EMERGENCY", "Datos de emergencia subidos: ${emergencyData.timestamp}")
                        scope.launch {
                            user.synced = true
                            db.appDao().markUserAsSynced(user)
                            motorcycle.synced = true
                            db.appDao().markMotorcycleAsSynced(motorcycle)
                            emergencyContact.synced = true
                            db.appDao().markEmergencyContactAsSynced(emergencyContact)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("EMERGENCY", "Error al subir datos de emergencia: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("EMERGENCY", "Error al crear datos de emergencia: ${e.message}")
            }
        }
    }

    private fun getLoggedInUserId(): String? {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("logged_in_user_id", null)
        if (userId == null) {
            Log.e("EMERGENCY", "No se encontró userId en SharedPreferences")
        } else {
            Log.d("EMERGENCY", "userId obtenido: $userId")
        }
        return userId
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
                description = "Canal para el servicio Bluetooth en segundo plano"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, "BluetoothServiceChannel")
            .setContentTitle("Servicio Bluetooth")
            .setContentText("Ejecutando en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serverSocket?.close()
        bluetoothSocket?.close()
        scope.cancel()
        Log.d("BluetoothService", "Servicio detenido")
    }
}

// Modelos
data class ReceivedSensorData(val timestamp: Long, val location: LocationData, val sensors: SensorsData)
data class EmergencyData(val eventType: String, val timestamp: Long, val location: LocationData, val sensors: SensorsData, val driver: UserData, val motorcycle: MotorcycleData, val route: RouteData)
data class UserData(val name: String, val phone: String, val emergencyContact: DataEmergencyContact)
data class MotorcycleData(val brand: String, val model: String, val plate: String, val color: String?)
data class RouteData(val startTimestamp: Long, val durationSoFar: Int)
data class LocationData(val lat: Double, val lng: Double)
data class SensorsData(val accel: AxisData, val gyro: AxisData, val speed: Double)
data class AxisData(val x: Double, val y: Double, val z: Double)