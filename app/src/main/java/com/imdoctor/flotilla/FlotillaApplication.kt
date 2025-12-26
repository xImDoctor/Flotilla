package com.imdoctor.flotilla

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.utils.LocaleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        // Применение сохраненного языка ПЕРЕД инициализацией Firebase
        initializeLocale()

        // Инициализация Firebase
        initializeFirebase()

        // Инициализация аудио системы
        initializeAudio()
    }

    /**
     * Инициализация локализации при старте приложения
     *
     * Загружает сохраненный язык из DataStore и применяет его
     */
    private fun initializeLocale() {
        val lifecycleScope = ProcessLifecycleOwner.get().lifecycleScope

        lifecycleScope.launch {
            // Читаем сохраненный язык
            val languageCode = AppContainer.settingsRepository.languageFlow.first()
            android.util.Log.d("FlotillaApplication", "initializeLocale: loaded language = $languageCode")

            // Применяем локаль при старте приложения
            LocaleManager.applyLocaleAtStartup(languageCode)
            android.util.Log.d("FlotillaApplication", "initializeLocale: locale applied")
        }
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

    /**
     * Инициализация аудио системы
     *
     * Подписывается на изменения настроек и запускает фоновую музыку
     */
    private fun initializeAudio() {
        val lifecycleScope = ProcessLifecycleOwner.get().lifecycleScope

        lifecycleScope.launch {
            // Подписываемся на изменения настроек музыки
            AppContainer.audioManager.observeMusicSetting(
                AppContainer.settingsRepository.musicEnabledFlow,
                this
            )

            // Подписываемся на изменения выбранного фонового трека
            AppContainer.audioManager.observeMusicTrackSetting(
                AppContainer.settingsRepository.musicTrackFlow,
                this
            )

            // Подписываемся на изменения настроек звуковых эффектов
            AppContainer.audioManager.observeSoundEffectsSetting(
                AppContainer.settingsRepository.soundEffectsEnabledFlow,
                this
            )

            // Подписываемся на изменения настроек вибрации
            AppContainer.vibrationManager.observeVibrationSetting(
                AppContainer.settingsRepository.vibrationEnabledFlow,
                this
            )

            // Небольшая задержка перед запуском музыки
            delay(100)

            // Запускаем фоновую музыку
            AppContainer.audioManager.startMusic()
        }
    }
}
