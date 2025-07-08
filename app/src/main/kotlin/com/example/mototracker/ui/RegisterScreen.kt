package com.example.mototracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(userId: Long, onNavigate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope() // Para lanzar corutinas manualmente

    // Función para registrar usuario (sin @Composable)
    fun registerUser() {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        // Lanza la operación asíncrona
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Verifica si el email ya existe
                    val existingUser = db.appDao().getUserByEmail(email)
                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "El email ya está registrado"
                        }
                        return@withContext
                    }

                    // Inserta el usuario
                    val user = User(name = name, email = email, phone = phone, password = password, company = "")
                    db.appDao().insertUser(user)
                    Log.d("Register", "Usuario registrado con email: $email")
                    withContext(Dispatchers.Main) {
                        errorMessage = "Registro exitoso"
                        onNavigate("login") // Navega tras éxito
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error al registrar usuario: ${e.message}"
                    Log.e("Register", "Error: ${e.message}")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(350.dp)
                .height(550.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD4F4FC)) // Color de fondo completo
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrarse",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 30.dp)
                )

                // Campo de nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter) {
                                focusManager.moveFocus(FocusDirection.Down) // Mueve al siguiente campo
                                true // Indica que el evento fue manejado
                            } else false
                        },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true, // Evita que el campo crezca verticalmente
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Campo de email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter) {
                                focusManager.moveFocus(FocusDirection.Down) // Mueve al siguiente campo
                                true
                            } else false
                        },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true, // Evita que el campo crezca
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Campo de teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter) {
                                focusManager.moveFocus(FocusDirection.Down) // Mueve al siguiente campo
                                true
                            } else false
                        },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true, // Evita que el campo crezca
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Campo de contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter) {
                                focusManager.moveFocus(FocusDirection.Down) // Mueve al siguiente campo o cierra
                                true
                            } else false
                        },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true, // Evita que el campo crezca
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), // Cierra el teclado
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )

                // Botón de registro
                Button(
                    onClick = { registerUser() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080))
                ) {
                    Text("Registrarse", color = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.padding(10.dp))

                // Mensaje de error
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Texto clicable para iniciar sesión
                Text(
                    text = "¿Ya tienes una cuenta? Iniciar sesión",
                    color = Color(0xFF008080),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onNavigate("login") }
                )
            }
        }
    }
}