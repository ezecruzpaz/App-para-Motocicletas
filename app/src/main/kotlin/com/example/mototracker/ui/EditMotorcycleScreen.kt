package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.mototracker.data.AppDatabase
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.mototracker.data.Motorcycle
import kotlinx.coroutines.runBlocking

@Composable
fun EditMotorcycleScreen(userId: Long, onNavigate: (String) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val db = AppDatabase.getInstance(LocalContext.current)
    var existingMotorcycle by remember { mutableStateOf<Motorcycle?>(null) }

    LaunchedEffect(userId) {
        val motorcycles = db.appDao().getMotorcyclesByUserId(userId)
        if (motorcycles.isNotEmpty()) {
            existingMotorcycle = motorcycles.first()
            brand = existingMotorcycle?.brand ?: ""
            model = existingMotorcycle?.model ?: ""
            plate = existingMotorcycle?.plate ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF8F9FC)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable { onNavigate("profile") },
                tint = Color(0xFF0D0F1C)
            )
            Text(
                text = "Registrar Motocicleta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D0F1C),
                modifier = Modifier.padding(start = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Marca") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Modelo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = plate,
            onValueChange = { plate = it },
            label = { Text("Placa") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Button(
            onClick = {
                if (brand.isNotEmpty() && model.isNotEmpty() && plate.isNotEmpty()) {
                    errorMessage = null
                    runBlocking {
                        if (existingMotorcycle == null) {
                            val motorcycle = Motorcycle(userId = userId, brand = brand, model = model, plate = plate)
                            db.appDao().insertMotorcycle(motorcycle)
                        } else {
                            val updatedMotorcycle = Motorcycle(userId = userId, brand = brand, model = model, plate = plate)
                            db.appDao().insertMotorcycle(updatedMotorcycle) // Reemplaza o actualiza
                        }
                        onNavigate("profile")
                    }
                } else {
                    errorMessage = "Por favor, completa todos los campos"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Guardar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}