package com.example.mototracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
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
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.data.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.withContext

@Composable
fun AddEmergencyContactScreen(navController: NavController, userId: String) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<EmergencyContact?>(null) }

    // Cargar contactos al iniciar
    LaunchedEffect(userId) {
        Log.d("AddEmergencyContact", "UserId recibido: $userId")
        scope.launch {
            withContext(Dispatchers.IO) {
                contacts = db.appDao().getEmergencyContactsForUser(userId.toLongOrNull() ?: 0)
            }
        }
    }

    // Función para guardar un nuevo contacto
    fun saveEmergencyContact() {
        if (fullName.isBlank() || fullName.length > 80) {
            errorMessage = "El nombre completo es requerido y debe tener hasta 80 caracteres"
            return
        }
        if (phoneNumber.isBlank() || phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }) {
            errorMessage = "El número de teléfono debe tener exactamente 10 dígitos"
            return
        }

        val contact = EmergencyContact(
            userId = userId.toLongOrNull() ?: 0L,
            fullName = fullName,
            phoneNumber = phoneNumber,
            relationship = relationship.ifEmpty { null },
            email = email.ifEmpty { null }
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.appDao().insertEmergencyContact(contact)
                }
                errorMessage = "Contacto guardado exitosamente"
                withContext(Dispatchers.IO) {
                    contacts = db.appDao().getEmergencyContactsForUser(userId.toLongOrNull() ?: 0)
                }
                showAddContactDialog = false
                fullName = ""
                phoneNumber = ""
                relationship = ""
                email = ""
            } catch (e: Exception) {
                errorMessage = "Error al guardar el contacto: ${e.message}"
                Log.e("AddEmergencyContact", "Error: ${e.message}")
            }
        }
    }

    // Función para actualizar un contacto
    fun updateEmergencyContact() {
        if (selectedContact == null) return
        if (fullName.isBlank() || fullName.length > 80) {
            errorMessage = "El nombre completo es requerido y debe tener hasta 80 caracteres"
            return
        }
        if (phoneNumber.isBlank() || phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }) {
            errorMessage = "El número de teléfono debe tener exactamente 10 dígitos"
            return
        }

        val updatedContact = selectedContact!!.copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            relationship = relationship.ifEmpty { null },
            email = email.ifEmpty { null }
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.appDao().updateEmergencyContact(updatedContact)
                }
                errorMessage = "Contacto actualizado exitosamente"
                withContext(Dispatchers.IO) {
                    contacts = db.appDao().getEmergencyContactsForUser(userId.toLongOrNull() ?: 0)
                }
                showEditContactDialog = false
                fullName = ""
                phoneNumber = ""
                relationship = ""
                email = ""
                selectedContact = null
            } catch (e: Exception) {
                errorMessage = "Error al actualizar el contacto: ${e.message}"
                Log.e("AddEmergencyContact", "Error: ${e.message}")
            }
        }
    }

    // Función para eliminar un contacto
    fun deleteEmergencyContact(contact: EmergencyContact) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.appDao().deleteEmergencyContact(contact)
                }
                errorMessage = "Contacto eliminado exitosamente"
                withContext(Dispatchers.IO) {
                    contacts = db.appDao().getEmergencyContactsForUser(userId.toLongOrNull() ?: 0)
                }
            } catch (e: Exception) {
                errorMessage = "Error al eliminar el contacto: ${e.message}"
                Log.e("AddEmergencyContact", "Error: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    text = "Contactos de Emergencia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D0F1C),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Mostrar contactos existentes o formulario
            if (contacts.isEmpty()) {
                // Formulario si no hay contactos
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { if (it.length <= 80) fullName = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = fullName.isBlank() && errorMessage != null,
                    singleLine = true
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                    label = { Text("Número de Teléfono") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = phoneNumber.length != 10 && errorMessage != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relación con el Conductor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = Color.White, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { saveEmergencyContact() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar Contacto", color = Color.White, fontSize = 16.sp)
                    }
                }
            } else {
                // Mostrar todos los contactos en un Card con LazyColumn
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8FFFFF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(contacts) { contact ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Contacto de Emergencia",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D0F1C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Nombre: ${contact.fullName}")
                                Text(text = "Teléfono: ${contact.phoneNumber}")
                                Text(text = "Relación: ${contact.relationship ?: "No especificada"}")
                                Text(text = "Email: ${contact.email ?: "No especificada"}")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            selectedContact = contact
                                            fullName = contact.fullName
                                            phoneNumber = contact.phoneNumber
                                            relationship = contact.relationship ?: ""
                                            email = contact.email ?: ""
                                            showEditContactDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar contacto",
                                            tint = Color(0xFF008080)
                                        )
                                    }
                                    IconButton(
                                        onClick = { deleteEmergencyContact(contact) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar contacto",
                                            tint = Color.Red
                                        )
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
                // Botón para agregar otro contacto (abre modal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showAddContactDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar otro contacto",
                            tint = Color(0xFF008080)
                        )
                    }
                }
            }

            // Mensaje de error
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }

        // Modal para agregar nuevo contacto
        if (showAddContactDialog) {
            AlertDialog(
                onDismissRequest = { showAddContactDialog = false },
                title = { Text("Agregar Nuevo Contacto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { if (it.length <= 80) fullName = it },
                            label = { Text("Nombre Completo") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            isError = fullName.isBlank() && errorMessage != null,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                            label = { Text("Número de Teléfono") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            isError = phoneNumber.length != 10 && errorMessage != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = relationship,
                            onValueChange = { relationship = it },
                            label = { Text("Relación con el Conductor") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            saveEmergencyContact()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar", color = Color.White, fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAddContactDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = Color.White, fontSize = 16.sp)
                    }
                }
            )
        }

        // Modal para editar contacto
        if (showEditContactDialog) {
            AlertDialog(
                onDismissRequest = { showEditContactDialog = false },
                title = { Text("Editar Contacto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { if (it.length <= 80) fullName = it },
                            label = { Text("Nombre Completo") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            isError = fullName.isBlank() && errorMessage != null,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                            label = { Text("Número de Teléfono") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            isError = phoneNumber.length != 10 && errorMessage != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = relationship,
                            onValueChange = { relationship = it },
                            label = { Text("Relación con el Conductor") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateEmergencyContact()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Actualizar", color = Color.White, fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showEditContactDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = Color.White, fontSize = 16.sp)
                    }
                }
            )
        }
    }
}