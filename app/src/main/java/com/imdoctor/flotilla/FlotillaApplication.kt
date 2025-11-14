package com.imdoctor.flotilla

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.imdoctor.flotilla.di.AppContainer

/**
 * Application класс для Flotilla
 * 
 * Инициализирует Firebase и другие глобальные компоненты
 */
class FlotillaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация DI контейнера
        AppContainer.initialize(this)
        
        // Инициализация Firebase
        initializeFirebase()
    }
    
    /**
     * Инициализация Firebase с настройками безопасности
     */
    private fun initializeFirebase() {
        // Firebase автоматически инициализируется из google-services.json
        FirebaseApp.initializeApp(this)
        
        // Настройки Firestore для оптимальной производительности
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // Кэширование данных оффлайн
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        
        firestore.firestoreSettings = settings
        
        // Включение Firebase Auth
        FirebaseAuth.getInstance()
    }
}
