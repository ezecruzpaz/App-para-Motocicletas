package com.example.mototracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.mototracker.data.AppDatabase
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

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
                        onNavigate = { newScreen ->
                            if (newScreen == "login") viewModel.clearUserId()
                            when (newScreen) {
                                "login", "editMotorcycle", "register" -> navController.navigate(newScreen)
                                else -> Log.w("Navigation", "Ruta no válida: $newScreen")
                            }
                        }
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
            }
        }
        scheduleSync()
    }

    private fun scheduleSync() {
        val targetTime = LocalTime.of(12, 0) // 12:00 PM
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