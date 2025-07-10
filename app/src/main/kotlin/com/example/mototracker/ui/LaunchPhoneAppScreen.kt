package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.example.mototracker.data.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun LaunchPhoneAppScreen(navController: NavController) {
    var isServerRunning by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Esperando conexión...") }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Placeholder para iniciar/parar el servidor Bluetooth
    fun startBluetoothServer() {
        scope.launch {
            try {
                // Lógica para iniciar el servidor Bluetooth (a implementar)
                Log.d("Bluetooth", "Iniciando servidor Bluetooth...")
                connectionStatus = "Conectando..."
                // Simulación de conexión (reemplazar con lógica real)
                // Ejemplo: delay(2000); connectionStatus = "Conectado"
                isServerRunning = true
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error al iniciar servidor: ${e.message}")
                connectionStatus = "Error al conectar"
            }
        }
    }

    fun stopBluetoothServer() {
        scope.launch {
            try {
                // Lógica para detener el servidor Bluetooth (a implementar)
                Log.d("Bluetooth", "Deteniendo servidor Bluetooth...")
                connectionStatus = "Servidor detenido"
                isServerRunning = false
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error al detener servidor: ${e.message}")
                connectionStatus = "Error al detener"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { navController.popBackStack() },
                    tint = Color(0xFF0D0F1C)
                )
                Text(
                    text = "Bluetooth Ruta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D0F1C),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Icono principal de Bluetooth
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                tint = Color(0xFF008080)
            )

            // Estado de conexión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF))
            ) {
                Text(
                    text = connectionStatus,
                    fontSize = 16.sp,
                    color = if (connectionStatus == "Conectado") Color(0xFF2E7D32) else Color(0xFF47569E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }

            // Botones de control
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { if (!isServerRunning) startBluetoothServer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
                    enabled = !isServerRunning // Habilitar solo si no está corriendo
                ) {
                    Text("Iniciar Ruta", color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = { if (isServerRunning) stopBluetoothServer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = isServerRunning // Habilitar solo si está corriendo
                ) {
                    Text("Terminar Ruta", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}