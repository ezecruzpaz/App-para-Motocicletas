package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.foundation.clickable

@Composable
fun EditUserScreen(userId: String, navController: NavController) {
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            try {
                val id = userId.toLongOrNull() ?: 0L
                userData = withContext(Dispatchers.IO) {
                    db.appDao().getUserById(id)
                }
                if (userData != null) {
                    Log.d("EditUser", "Datos cargados: User=$userData")
                } else {
                    Log.w("EditUser", "No se encontró el usuario con userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("EditUser", "Error al cargar datos: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            Log.w("EditUser", "userId está vacío")
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
                .padding(16.dp)
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
                    text = "Editar Perfil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D0F1C),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Nombre: ${userData?.name ?: "N/A"}",
                            fontSize = 16.sp,
                            color = Color(0xFF0D0F1C),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "Teléfono: ${userData?.phone?.let { "+52 $it" } ?: "N/A"}",
                            fontSize = 16.sp,
                            color = Color(0xFF0D0F1C),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Email: ${userData?.email ?: "N/A"}",
                            fontSize = 16.sp,
                            color = Color(0xFF0D0F1C),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Compañía: ${userData?.company ?: "N/A"}",
                            fontSize = 16.sp,
                            color = Color(0xFF0D0F1C),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}