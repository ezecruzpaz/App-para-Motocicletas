package com.example.mototracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.mototracker.data.AppDatabase
import com.example.mototracker.ui.AddEmergencyContactScreen
import com.example.mototracker.ui.LaunchPhoneAppScreen
import com.example.mototracker.ui.LoginScreen
import com.example.mototracker.ui.ProfileScreen
import com.example.mototracker.ui.EditMotorcycleScreen
import com.example.mototracker.ui.RegisterScreen
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.delay
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    var userId: Long = 0L
        private set

    fun setUserId(id: Long) {
        userId = id
    }

    fun clearUserId() {
        userId = 0L
    }
}

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.getInstance(this) }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "Todos los permisos otorgados")
        } else {
            Log.w("MainActivity", "Algunos permisos fueron denegados")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        requestPermissions()

        setContent {
            val navController = rememberNavController()
            val viewModel: MainViewModel = viewModel()

            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { userId ->
                            viewModel.setUserId(userId)
                            navController.navigate("profile") {
                                popUpTo("login") { inclusive = false }
                            }
                        },
                        onNavigate = { destination ->
                            navController.navigate(destination) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            }
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        userId = viewModel.userId.toString(),
                        navController = navController
                    )
                }
                composable("editMotorcycle") {
                    EditMotorcycleScreen(
                        userId = viewModel.userId,
                        onNavigate = { newScreen ->
                            if (newScreen == "login") viewModel.clearUserId()
                            navController.navigate(newScreen)
                        }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        userId = viewModel.userId,
                        onNavigate = { newScreen ->
                            if (newScreen == "login") viewModel.clearUserId()
                            navController.navigate(newScreen)
                        }
                    )
                }
                composable("launchPhoneApp") {
                    LaunchPhoneAppScreen(navController = navController)
                }
                composable("addEmergencyContact/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
                    AddEmergencyContactScreen(navController = navController, userId = userId.toString())
                }
            }
        }
        scheduleSync()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    private fun scheduleSync() {
        val targetTime = LocalTime.of(12, 0)
        val now = LocalDateTime.now()
        val target = if (now.toLocalTime().isBefore(targetTime)) {
            now.withHour(targetTime.hour).withMinute(targetTime.minute)
        } else {
            now.plusDays(1).withHour(targetTime.hour).withMinute(targetTime.minute)
        }
        val delay = Duration.between(now, target).toMillis()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "syncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD4F4FC)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_image),
            contentDescription = "Splash Screen",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )
    }
}