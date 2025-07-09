package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.data.User
import com.example.mototracker.data.AppDao.UserWithMotorcycles
import com.example.mototracker.data.Motorcycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(userId: String, onNavigate: (String) -> Unit) {
    var userData by remember { mutableStateOf<User?>(null) }
    var motorcycleData by remember { mutableStateOf<Motorcycle?>(null) } // Para almacenar la primera motocicleta
    var isLoading by remember { mutableStateOf(true) } // Estado de carga
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            try {
                val id = userId.toLongOrNull() ?: 0L // Convertir userId a Long
                val userWithMotorcycles = withContext(Dispatchers.IO) {
                    db.appDao().getUserWithMotorcycles(id)
                }
                userData = userWithMotorcycles?.user
                motorcycleData = userWithMotorcycles?.motorcycles?.firstOrNull() // Tomar la primera motocicleta
                if (userData != null) {
                    Log.d("Profile", "Datos cargados: User=$userData, Motorcycle=$motorcycleData")
                } else {
                    Log.w("Profile", "No se encontró el usuario con userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("Profile", "Error al cargar datos: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            Log.w("Profile", "userId está vacío")
            isLoading = false
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
                .padding(bottom = 60.dp) // Espacio para el footer
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FC))
                    .padding(16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { onNavigate("login") },
                    tint = Color(0xFF0D0F1C)
                )
                Text(
                    text = "Perfil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D0F1C),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Profile Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                } else {
                    AsyncImage(
                        model = "https://img.freepik.com/vector-premium/hombre-negro-casco-esqui-icono-vector_960391-628.jpg",
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userData?.name ?: "N/A",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D0F1C),
                        textAlign = TextAlign.Center // Centrar texto
                    )
                    Text(
                        text = userData?.phone?.let { "+52 $it" } ?: "+1 (N/A)",
                        fontSize = 16.sp,
                        color = Color(0xFF0D0F1C),
                        textAlign = TextAlign.Center // Centrar texto
                    )
                    Text(
                        text = userData?.email ?: "N/A",
                        fontSize = 16.sp,
                        color = Color(0xFF0D0F1C),
                        textAlign = TextAlign.Center // Centrar texto
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Motorcycle Section
            Text(
                text = "Motocicleta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D0F1C),
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
                textAlign = TextAlign.Start // Alinear texto a la izquierda
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start // Alinear a la izquierda
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                } else if (motorcycleData == null) {
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF)) // Color de fondo de la card
                        ) {
                            Text(
                                text = "No motorcycle registered yet.",
                                fontSize = 16.sp,
                                color = Color(0xFF47569E),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        FloatingActionButton(
                            onClick = { onNavigate("editMotorcycle") },
                            modifier = Modifier
                                .size(48.dp) // Tamaño
                                .align(Alignment.TopEnd)
                                .offset(y = (-18.dp), x = (8.dp)), // Parte arriba y un poco adentro
                            containerColor = Color(0xFF008080),
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Registrar Motocicleta",
                                modifier = Modifier.size(24.dp), // Ícono
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF)) // Color de fondo de la card
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${motorcycleData?.brand ?: "N/A"} ${motorcycleData?.model ?: "N/A"}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black, // Nuevo color de texto
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = "Placa: ${motorcycleData?.plate ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black, // Nuevo color de texto
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Año: ${motorcycleData?.year?.toString() ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black, // Nuevo color de texto
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Cilindrada: ${motorcycleData?.displacement?.toString() ?: "N/A"} cc",
                                        fontSize = 14.sp,
                                        color = Color.Black, // Nuevo color de texto
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Seguro: ${motorcycleData?.insurance ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black, // Nuevo color de texto
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                // Imagen preestablecida a la derecha (cuadrada)
                                AsyncImage(
                                    model = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSBIxa2Hwh8FFUJDGeFhubWEtgb9w9mZ5lhN8JnA7_fjRYyqKh-XDksKyx43xsZxu0USYU&usqp=CAU",
                                    contentDescription = "Motocycle Image",
                                    modifier = Modifier
                                        .size(94.dp), // Forma cuadrada sin clip(CircleShape)
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { onNavigate("editMotorcycle") },
                            modifier = Modifier
                                .size(48.dp) // Tamaño
                                .align(Alignment.TopEnd)
                                .offset(y = (-18.dp), x = (8.dp)), // Parte arriba y un poco adentro
                            containerColor = Color(0xFF39D8D4),
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Motocicleta",
                                modifier = Modifier.size(24.dp), // Ícono
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FC))
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color(0xFF0D0F1C),
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Users",
                    tint = Color(0xFF47569E),
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map",
                    tint = Color(0xFF47569E),
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF47569E),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}