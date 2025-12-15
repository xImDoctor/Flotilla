package com.imdoctor.flotilla.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
        private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
        private val MUSIC_TRACK_KEY = stringPreferencesKey("music_track")
        private val SOUND_EFFECTS_ENABLED_KEY = booleanPreferencesKey("sound_effects_enabled")
        private val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val SELECTED_SHIP_SKIN_KEY = stringPreferencesKey("selected_ship_skin")

        // Дефолтные значения
        private const val DEFAULT_NICKNAME = "Player"
        private const val DEFAULT_SHOW_COORDINATES = false  // Временно отключено (в доработке)
        private const val DEFAULT_MUSIC_ENABLED = true
        private const val DEFAULT_MUSIC_TRACK = "cats_cradle"
        private const val DEFAULT_SOUND_EFFECTS_ENABLED = true
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
     * Flow с настройкой фоновой музыки
     */
    val musicEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: DEFAULT_MUSIC_ENABLED
        }

    /**
     * Flow с выбранным фоновым треком
     */
    val musicTrackFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_TRACK_KEY] ?: DEFAULT_MUSIC_TRACK
        }

    /**
     * Flow с настройкой звуковых эффектов
     */
    val soundEffectsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SOUND_EFFECTS_ENABLED_KEY] ?: DEFAULT_SOUND_EFFECTS_ENABLED
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
     * Сохранение настройки фоновой музыки
     *
     * @param enabled Включена ли фоновая музыка
     */
    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    /**
     * Сохранение выбранного фонового трека
     *
     * @param trackId ID трека ("cats_cradle" или "hibiscus")
     */
    suspend fun setMusicTrack(trackId: String) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_TRACK_KEY] = trackId
        }
    }

    /**
     * Сохранение настройки звуковых эффектов
     *
     * @param enabled Включены ли звуковые эффекты
     */
    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_ENABLED_KEY] = enabled
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
     * Сброс всех настроек к дефолтным значениям (кроме никнейма)
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            // Сохраняем никнейм
            val currentNickname = preferences[NICKNAME_KEY]

            // Очищаем всё
            preferences.clear()

            // Восстанавливаем никнейм
            currentNickname?.let {
                preferences[NICKNAME_KEY] = it
            }
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
            musicEnabled = preferences[MUSIC_ENABLED_KEY] ?: DEFAULT_MUSIC_ENABLED,
            musicTrack = preferences[MUSIC_TRACK_KEY] ?: DEFAULT_MUSIC_TRACK,
            soundEffectsEnabled = preferences[SOUND_EFFECTS_ENABLED_KEY] ?: DEFAULT_SOUND_EFFECTS_ENABLED,
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
    val musicEnabled: Boolean,
    val musicTrack: String,
    val soundEffectsEnabled: Boolean,
    val animationsEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val selectedShipSkin: String
)
