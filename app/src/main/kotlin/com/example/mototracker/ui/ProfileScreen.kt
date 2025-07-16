package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mototracker.MainViewModel
import com.example.mototracker.R
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.data.User
import com.example.mototracker.data.AppDao.UserWithMotorcycles
import com.example.mototracker.data.Motorcycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(userId: String, navController: NavController) {
    var userData by remember { mutableStateOf<User?>(null) }
    var motorcycleData by remember { mutableStateOf<Motorcycle?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel() // Inyectar MainViewModel
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            try {
                val id = userId.toLongOrNull() ?: 0L
                val userWithMotorcycles = withContext(Dispatchers.IO) {
                    db.appDao().getUserWithMotorcycles(id)
                }
                userData = userWithMotorcycles?.user
                motorcycleData = userWithMotorcycles?.motorcycles?.firstOrNull()
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
                .padding(bottom = 60.dp)
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
                    modifier = Modifier.clickable { navController.navigate("login") },
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
                horizontalAlignment = Alignment.CenterHorizontally
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
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = userData?.phone?.let { "+52 $it" } ?: "+1 (N/A)",
                        fontSize = 16.sp,
                        color = Color(0xFF0D0F1C),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = userData?.email ?: "N/A",
                        fontSize = 16.sp,
                        color = Color(0xFF0D0F1C),
                        textAlign = TextAlign.Center
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
                textAlign = TextAlign.Start
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF))
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
                            onClick = { navController.navigate("editMotorcycle") },
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopEnd)
                                .offset(y = (-18.dp), x = (8.dp)),
                            containerColor = Color(0xFF008080),
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Registrar Motocicleta",
                                modifier = Modifier.size(24.dp),
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF))
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
                                        color = Color.Black,
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = "Placa: ${motorcycleData?.plate ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Año: ${motorcycleData?.year?.toString() ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Cilindrada: ${motorcycleData?.displacement?.toString() ?: "N/A"} cc",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "Seguro: ${motorcycleData?.insurance ?: "N/A"}",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                AsyncImage(
                                    model = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSBIxa2Hwh8FFUJDGeFhubWEtgb9w9mZ5lhN8JnA7_fjRYyqKh-XDksKyx43xsZxu0USYU&usqp=CAU",
                                    contentDescription = "Motocycle Image",
                                    modifier = Modifier
                                        .size(94.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { navController.navigate("editMotorcycle") },
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopEnd)
                                .offset(y = (-18.dp), x = (8.dp)),
                            containerColor = Color(0xFF39D8D4),
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Motocicleta",
                                modifier = Modifier.size(24.dp),
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
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF39D8D4),
                    modifier = Modifier
                        .size(24.dp)

                )
                Image(
                    painter = painterResource(id = R.drawable.emergencia),
                    contentDescription = "Contacto de Emergencia",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            Log.d("ProfileScreen", "Navegando con userId: $userId")
                            if (userId.toLongOrNull() ?: 0 > 0) {
                                navController.navigate("addEmergencyContact/$userId")
                            } else {
                                Log.e("ProfileScreen", "userId es inválido: $userId")
                            }
                        },
                    colorFilter = ColorFilter.tint(Color(0xFF39D8D4))
                )
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "rutas",
                    tint = Color(0xFF39D8D4),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("launchPhoneApp") }
                )
            }
        }
    }
}