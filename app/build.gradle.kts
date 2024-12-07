plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dam2jms.gestiongastosapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dam2jms.gestiongastosapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Dependencias de Jetpack Compose y otras
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose Material 3
    implementation("androidx.compose.material:material:1.7.5")

    // Navegación entre pantallas con Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // Firebase BoM: gestionar versiones de Firebase (esto maneja las versiones de las dependencias de Firebase)
    implementation(platform("com.google.firebase:firebase-bom:31.2.3"))

    // Firebase Auth y Firestore: sin versiones explícitas, se usa la versión del BoM de Firebase
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Si no estás utilizando Firebase Storage explícitamente, no es necesario incluir esta dependencia.
    // implementation("com.google.firebase:firebase-storage-ktx")

    // Para nuevos iconos en Compose
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit y OkHttp para llamadas a APIs
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    // Para gráficos en Compose
    implementation("com.patrykandpatrick.vico:compose:1.6.5")

    // Para gráficos en Android
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Dependencias de Compose
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material:material:1.7.5")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")

    // Coil para imágenes en Compose
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.google.firebase:firebase-storage-ktx")

    // Ktor for networking (if not using Retrofit)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Logging Interceptor for Retrofit/OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Additional Retrofit converters (optional)
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")


}
