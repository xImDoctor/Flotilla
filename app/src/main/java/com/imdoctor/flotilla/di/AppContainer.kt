package com.imdoctor.flotilla.di

import android.content.Context
import com.imdoctor.flotilla.data.local.preferences.SettingsDataStore
import com.imdoctor.flotilla.data.remote.firebase.FirebaseAuthManager
import com.imdoctor.flotilla.data.remote.firebase.FirestoreManager
import com.imdoctor.flotilla.data.remote.websocket.WebSocketManager
import com.imdoctor.flotilla.data.repository.SettingsRepository
import com.imdoctor.flotilla.data.repository.UserRepository

/**
 * Простой Dependency Injection контейнер
 * 
 * Создаёт и хранит синглтоны всех зависимостей приложения.
 * Используется вместо полноценного DI фреймворка (Hilt/Koin) для простоты.
 * 
 * БЕЗОПАСНОСТЬ:
 * - Все менеджеры создаются как синглтоны
 * - Context безопасно хранится (Application Context)
 */
object AppContainer {
    
    private var applicationContext: Context? = null
    
    /**
     * Инициализация контейнера
     * 
     * Вызывается в FlotillaApplication.onCreate()
     * 
     * @param context Application Context
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    

    // МЕНЕДЖЕРЫ (синглтоны)
    /**
     * Firebase Authentication Manager
     */
    val authManager: FirebaseAuthManager by lazy {
        FirebaseAuthManager()
    }
    
    /**
     * Firestore Manager
     */
    val firestoreManager: FirestoreManager by lazy {
        FirestoreManager()
    }
    
    /**
     * Local Settings DataStore
     */
    val settingsDataStore: SettingsDataStore by lazy {
        requireNotNull(applicationContext) {
            "AppContainer not initialized. Call initialize() in Application.onCreate()"
        }
        SettingsDataStore(applicationContext!!)
    }

    /**
     * WebSocket Manager для онлайн игр
     */
    val webSocketManager: WebSocketManager by lazy {
        WebSocketManager()
    }


    // РЕПОЗИТОРИИ (синглтоны)
    /**
     * Settings Repository
     */
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            localDataStore = settingsDataStore,
            firestoreManager = firestoreManager,
            authManager = authManager
        )
    }
    
    /**
     * User Repository (профиль и статистика)
     */
    val userRepository: UserRepository by lazy {
        UserRepository(
            firestoreManager = firestoreManager,
            authManager = authManager
        )
    }
}
