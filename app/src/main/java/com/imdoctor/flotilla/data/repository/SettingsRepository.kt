package com.imdoctor.flotilla.data.repository

import com.imdoctor.flotilla.data.local.preferences.SettingsDataStore
import com.imdoctor.flotilla.data.remote.firebase.FirebaseAuthManager
import com.imdoctor.flotilla.data.remote.firebase.FirestoreManager
import com.imdoctor.flotilla.data.remote.firebase.models.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Исключения для операций с никнеймом
 */
sealed class NicknameChangeException(message: String) : Exception(message) {
    object TimeoutNotExpired : NicknameChangeException("Можно менять никнейм не чаще одного раза в 5 минут")
    object NicknameAlreadyTaken : NicknameChangeException("Этот никнейм уже занят другим пользователем")
    object InvalidNickname : NicknameChangeException("Никнейм должен содержать от 3 до 20 символов")
    object NetworkError : NicknameChangeException("Ошибка сети. Проверьте подключение к интернету")
}

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

    companion object {
        // Таймаут на изменение никнейма: 5 минут
        private val NICKNAME_CHANGE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5)

        // Ограничения на никнейм
        private const val MIN_NICKNAME_LENGTH = 3
        private const val MAX_NICKNAME_LENGTH = 20
    }
    
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
     * Сохранение никнейма с валидацией
     *
     * Проверяет:
     * - Таймаут (прошло ли 5 минут с последнего изменения)
     * - Уникальность (не занят ли никнейм другим пользователем)
     * - Валидность (длина, допустимые символы)
     *
     * Обновляет:
     * - UserProfile (основной профиль)
     * - UserSettings (настройки)
     * - Локальное хранилище (DataStore)
     *
     * @param nickname Новый никнейм
     * @return Result с успехом или ошибкой
     */
    suspend fun setNickname(nickname: String): Result<Unit> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))

            // 1. Валидация никнейма
            val trimmedNickname = nickname.trim()
            if (trimmedNickname.length < MIN_NICKNAME_LENGTH ||
                trimmedNickname.length > MAX_NICKNAME_LENGTH) {
                return Result.failure(NicknameChangeException.InvalidNickname)
            }

            // 2. Получаем текущий профиль пользователя
            val currentProfile = firestoreManager.getUserProfile(userId).getOrNull()
                ?: return Result.failure(Exception("Profile not found"))

            // 3. Если никнейм не изменился, просто обновляем локально
            if (currentProfile.nickname == trimmedNickname) {
                localDataStore.setNickname(trimmedNickname)
                return Result.success(Unit)
            }

            // 4. Проверка таймаута (прошло ли 5 минут с последнего изменения)
            currentProfile.lastNicknameChange?.let { lastChange ->
                val timeSinceLastChange = System.currentTimeMillis() - lastChange.time
                if (timeSinceLastChange < NICKNAME_CHANGE_TIMEOUT_MILLIS) {
                    return Result.failure(NicknameChangeException.TimeoutNotExpired)
                }
            }

            // 5. Проверка уникальности никнейма
            val isAvailable = firestoreManager.checkNicknameAvailability(trimmedNickname, userId)
                .getOrElse {
                    return Result.failure(NicknameChangeException.NetworkError)
                }

            if (!isAvailable) {
                return Result.failure(NicknameChangeException.NicknameAlreadyTaken)
            }

            // 6. Обновляем профиль с новым никнеймом и timestamp
            val updatedProfile = currentProfile.copy(
                nickname = trimmedNickname,
                lastNicknameChange = Date()
            )

            firestoreManager.createOrUpdateUserProfile(updatedProfile)
                .getOrElse {
                    return Result.failure(NicknameChangeException.NetworkError)
                }

            // 7. Сохраняем локально
            localDataStore.setNickname(trimmedNickname)

            // 8. Синхронизируем настройки с Firebase
            syncSettingsToFirebase(userId)

            Result.success(Unit)
        } catch (e: NicknameChangeException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(NicknameChangeException.NetworkError)
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
