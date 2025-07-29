plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Asegúrate de incluir este plugin
    id("org.jetbrains.kotlin.kapt") // Añade el plugin kapt para Kotlin
}

android {
    namespace = "com.example.mototracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mototracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // BOM de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Room dependencies (usar una sola versión consistente)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.google.firebase.database.ktx)
    implementation(libs.support.annotations)
    kapt("androidx.room:room-compiler:2.6.1") // Usa kapt para Kotlin
    implementation("androidx.room:room-ktx:2.6.1") // Añade esta línea

    // Jetpack Compose y otras dependencias
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Usa una sola versión
    implementation(libs.firebase.firestore.ktx)
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation(libs.androidx.work.runtime.ktx)

    // Jetpack Compose (usa versiones consistentes)
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.material3:material3:1.2.1") // Elige una versión y elimina duplicados
    implementation("io.coil-kt:coil-compose:2.7.0") // Usa la última versión
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Exportar CSV
    implementation("com.opencsv:opencsv:5.9")

    // Compatibilidad con java.time
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")

    //image
    implementation ("io.coil-kt:coil-compose:2.4.0" )// Para AsyncImage
    implementation( "androidx.activity:activity-compose:1.7.2")// Para ActivityResultLauncher
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ViewModel con Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation ("cn.pedant.sweetalert:library:1.3.1")

    implementation(libs.retrofit.gson)
}