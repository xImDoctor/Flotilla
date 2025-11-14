package com.imdoctor.flotilla.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Менеджер для работы с локальными настройками через DataStore
 * 
 * DataStore - современная замена SharedPreferences с:
 * - Асинхронным API
 * - Type safety
 * - Поддержкой Flow
 * 
 * Используется для хранения настроек, которые должны быть
 * доступны оффлайн и быстро загружаться.
 */

// Extension для создания DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flotilla_settings"
)

class SettingsDataStore(private val context: Context) {
    
    companion object {
        // Ключи для настроек
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val SHOW_COORDINATES_KEY = booleanPreferencesKey("show_coordinates")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val SELECTED_SHIP_SKIN_KEY = stringPreferencesKey("selected_ship_skin")
        
        // Дефолтные значения
        private const val DEFAULT_NICKNAME = "Player"
        private const val DEFAULT_SHOW_COORDINATES = true
        private const val DEFAULT_SOUND_ENABLED = true
        private const val DEFAULT_ANIMATIONS_ENABLED = true
        private const val DEFAULT_VIBRATION_ENABLED = true
        private const val DEFAULT_SHIP_SKIN = "default"
    }
    
    // ========================================
    // FLOWS для чтения настроек
    // ========================================
    
    /**
     * Flow с никнеймом пользователя
     */
    val nicknameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[NICKNAME_KEY] ?: DEFAULT_NICKNAME
        }
    
    /**
     * Flow с настройкой показа координат
     */
    val showCoordinatesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_COORDINATES_KEY] ?: DEFAULT_SHOW_COORDINATES
        }
    
    /**
     * Flow с настройкой звука
     */
    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: DEFAULT_SOUND_ENABLED
        }
    
    /**
     * Flow с настройкой анимаций
     */
    val animationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ANIMATIONS_ENABLED_KEY] ?: DEFAULT_ANIMATIONS_ENABLED
        }
    
    /**
     * Flow с настройкой вибрации
     */
    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[VIBRATION_ENABLED_KEY] ?: DEFAULT_VIBRATION_ENABLED
        }
    
    /**
     * Flow с выбранным скином кораблей
     */
    val selectedShipSkinFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_SHIP_SKIN_KEY] ?: DEFAULT_SHIP_SKIN
        }
    
    // ========================================
    // МЕТОДЫ для сохранения настроек
    // ========================================
    
    /**
     * Сохранение никнейма
     * 
     * @param nickname Новый никнейм
     */
    suspend fun setNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[NICKNAME_KEY] = nickname
        }
    }
    
    /**
     * Сохранение настройки показа координат
     * 
     * @param show Показывать ли координаты
     */
    suspend fun setShowCoordinates(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_COORDINATES_KEY] = show
        }
    }
    
    /**
     * Сохранение настройки звука
     * 
     * @param enabled Включены ли звуковые эффекты
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Сохранение настройки анимаций
     * 
     * @param enabled Включены ли анимации
     */
    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANIMATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Сохранение настройки вибрации
     * 
     * @param enabled Включена ли вибрация
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Сохранение выбранного скина кораблей
     * 
     * @param skinId ID скина
     */
    suspend fun setSelectedShipSkin(skinId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SHIP_SKIN_KEY] = skinId
        }
    }
    
    /**
     * Сброс всех настроек к дефолтным значениям
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Получение текущих настроек разом (suspend функция)
     */
    suspend fun getCurrentSettings(): SettingsSnapshot {
        val preferences = context.dataStore.data.map { it }.first()
        
        return SettingsSnapshot(
            nickname = preferences[NICKNAME_KEY] ?: DEFAULT_NICKNAME,
            showCoordinates = preferences[SHOW_COORDINATES_KEY] ?: DEFAULT_SHOW_COORDINATES,
            soundEnabled = preferences[SOUND_ENABLED_KEY] ?: DEFAULT_SOUND_ENABLED,
            animationsEnabled = preferences[ANIMATIONS_ENABLED_KEY] ?: DEFAULT_ANIMATIONS_ENABLED,
            vibrationEnabled = preferences[VIBRATION_ENABLED_KEY] ?: DEFAULT_VIBRATION_ENABLED,
            selectedShipSkin = preferences[SELECTED_SHIP_SKIN_KEY] ?: DEFAULT_SHIP_SKIN
        )
    }
}

/**
 * Snapshot текущих настроек
 * 
 * Используется для одноразового чтения всех настроек
 */
data class SettingsSnapshot(
    val nickname: String,
    val showCoordinates: Boolean,
    val soundEnabled: Boolean,
    val animationsEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val selectedShipSkin: String
)
