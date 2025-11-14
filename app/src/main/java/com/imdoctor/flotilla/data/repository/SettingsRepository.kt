package com.imdoctor.flotilla.data.repository

import com.imdoctor.flotilla.data.local.preferences.SettingsDataStore
import com.imdoctor.flotilla.data.remote.firebase.FirebaseAuthManager
import com.imdoctor.flotilla.data.remote.firebase.FirestoreManager
import com.imdoctor.flotilla.data.remote.firebase.models.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository для управления настройками пользователя
 * 
 * Обеспечивает:
 * - Локальное хранение через DataStore (быстрый доступ оффлайн)
 * - Синхронизацию с Firebase (доступ с разных устройств)
 * - Единый API для работы с настройками
 */
class SettingsRepository(
    private val localDataStore: SettingsDataStore,
    private val firestoreManager: FirestoreManager,
    private val authManager: FirebaseAuthManager
) {
    
    // ========================================
    // FLOWS для чтения настроек
    // ========================================
    
    val nicknameFlow: Flow<String> = localDataStore.nicknameFlow
    val showCoordinatesFlow: Flow<Boolean> = localDataStore.showCoordinatesFlow
    val soundEnabledFlow: Flow<Boolean> = localDataStore.soundEnabledFlow
    val animationsEnabledFlow: Flow<Boolean> = localDataStore.animationsEnabledFlow
    val vibrationEnabledFlow: Flow<Boolean> = localDataStore.vibrationEnabledFlow
    val selectedShipSkinFlow: Flow<String> = localDataStore.selectedShipSkinFlow
    
    // ========================================
    // МЕТОДЫ для сохранения настроек
    // ========================================
    
    /**
     * Сохранение никнейма
     * 
     * Сохраняет локально и синхронизирует с Firebase
     */
    suspend fun setNickname(nickname: String): Result<Unit> {
        return try {
            // Сохраняем локально
            localDataStore.setNickname(nickname)
            
            // Синхронизируем с Firebase
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение настройки показа координат
     */
    suspend fun setShowCoordinates(show: Boolean): Result<Unit> {
        return try {
            localDataStore.setShowCoordinates(show)
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение настройки звука
     */
    suspend fun setSoundEnabled(enabled: Boolean): Result<Unit> {
        return try {
            localDataStore.setSoundEnabled(enabled)
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение настройки анимаций
     */
    suspend fun setAnimationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            localDataStore.setAnimationsEnabled(enabled)
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение настройки вибрации
     */
    suspend fun setVibrationEnabled(enabled: Boolean): Result<Unit> {
        return try {
            localDataStore.setVibrationEnabled(enabled)
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение выбранного скина кораблей
     */
    suspend fun setSelectedShipSkin(skinId: String): Result<Unit> {
        return try {
            localDataStore.setSelectedShipSkin(skinId)
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================
    // СИНХРОНИЗАЦИЯ
    // ========================================
    
    /**
     * Синхронизация настроек из DataStore в Firebase
     * 
     * Вызывается автоматически после каждого изменения настроек
     */
    private suspend fun syncSettingsToFirebase(userId: String): Result<Unit> {
        return try {
            val snapshot = localDataStore.getCurrentSettings()
            
            val userSettings = UserSettings(
                userId = userId,
                nickname = snapshot.nickname,
                showCoordinates = snapshot.showCoordinates,
                soundEnabled = snapshot.soundEnabled,
                animationsEnabled = snapshot.animationsEnabled,
                vibrationEnabled = snapshot.vibrationEnabled,
                selectedShipSkin = snapshot.selectedShipSkin
            )
            
            firestoreManager.saveUserSettings(userSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Загрузка настроек из Firebase в DataStore
     * 
     * Используется при первом запуске или при смене устройства
     */
    suspend fun loadSettingsFromFirebase(): Result<Unit> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            val result = firestoreManager.getUserSettings(userId)
            
            result.getOrNull()?.let { settings ->
                // Загружаем настройки в DataStore
                localDataStore.setNickname(settings.nickname)
                localDataStore.setShowCoordinates(settings.showCoordinates)
                localDataStore.setSoundEnabled(settings.soundEnabled)
                localDataStore.setAnimationsEnabled(settings.animationsEnabled)
                localDataStore.setVibrationEnabled(settings.vibrationEnabled)
                localDataStore.setSelectedShipSkin(settings.selectedShipSkin)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Принудительная синхронизация
     * 
     * Отправляет текущие локальные настройки в Firebase
     */
    suspend fun forceSyncToFirebase(): Result<Unit> {
        return authManager.currentUserId?.let { userId ->
            syncSettingsToFirebase(userId)
        } ?: Result.failure(Exception("User not authenticated"))
    }
    
    /**
     * Сброс настроек к дефолтным значениям
     */
    suspend fun resetToDefaults(): Result<Unit> {
        return try {
            localDataStore.resetToDefaults()
            
            authManager.currentUserId?.let { userId ->
                syncSettingsToFirebase(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
