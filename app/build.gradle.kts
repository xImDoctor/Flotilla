import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // Google Services plugin для обработки google-services.json
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.imdoctor.flotilla"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.imdoctor.flotilla"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.7.5b"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Читаем конфигурацию сервера из local.properties
        val localProperties = File(rootProject.projectDir, "local.properties")
        val properties = Properties()

        if (localProperties.exists()) {
            properties.load(localProperties.inputStream())
        }

        // Добавляем в BuildConfig (доступно в коде как BuildConfig.SERVER_LOCAL_IP)
        buildConfigField(
            "String",
            "SERVER_LOCAL_IP",
            "\"${properties.getProperty("flotilla.server.local.ip", "192.168.1.100")}\""
        )
        buildConfigField(
            "int",
            "SERVER_LOCAL_PORT",
            properties.getProperty("flotilla.server.local.port", "8000")
        )
        buildConfigField(
            "String",
            "SERVER_PRODUCTION_URL",
            "\"${properties.getProperty("flotilla.server.production.url", "https://flotilla-server.com")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // Включаем обфускацию для релиза
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
        buildConfig = true  // Для BuildConfig (для хранения констант)
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation & Lifecycle
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Firebase (используем BOM для управления версиями)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)         // Аутентификация
    implementation(libs.firebase.firestore)    // Cloud Firestore для БД
    // implementation(libs.firebase.analytics)    // Analytics
    // implementation(libs.firebase.crashlytics) // Crashlytics (позже)

    // DataStore для локального хранения настроек
    implementation(libs.androidx.datastore.preferences)

    // WebSocket & HTTP клиент
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)  // Kotlinx Serialization для WebSocket
    implementation("com.google.code.gson:gson:2.10.1")  // Gson для Firebase (может понадобиться)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
