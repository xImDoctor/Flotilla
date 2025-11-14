package com.imdoctor.flotilla.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана настроек
 * 
 * Управляет состоянием настроек и их сохранением
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // STATE FLOWS для UI
    
    /**
     * Никнейм пользователя
     */
    val nickname: StateFlow<String> = settingsRepository.nicknameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Player"
        )
    
    /**
     * Показывать координаты на сетке
     */
    val showCoordinates: StateFlow<Boolean> = settingsRepository.showCoordinatesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    /**
     * Звуковые эффекты включены
     */
    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    /**
     * Анимации включены
     */
    val animationsEnabled: StateFlow<Boolean> = settingsRepository.animationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    /**
     * Вибрация включена
     */
    val vibrationEnabled: StateFlow<Boolean> = settingsRepository.vibrationEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    

    // ДЕЙСТВИЯ
    
    /**
     * Обновление никнейма
     * 
     * @param newNickname Новый никнейм
     */
    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            settingsRepository.setNickname(newNickname)
        }
    }
    
    /**
     * Переключение настройки координат
     * 
     * @param show Показывать ли координаты
     */
    fun toggleShowCoordinates(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowCoordinates(show)
        }
    }
    
    /**
     * Переключение звуковых эффектов
     * 
     * @param enabled Включены ли звуки
     */
    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }
    
    /**
     * Переключение анимаций
     * 
     * @param enabled Включены ли анимации
     */
    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnimationsEnabled(enabled)
        }
    }
    
    /**
     * Переключение вибрации
     * 
     * @param enabled Включена ли вибрация
     */
    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }
    
    /**
     * Сброс настроек к дефолтным значениям
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }
    
    /**
     * Принудительная синхронизация настроек с Firebase
     */
    fun syncSettings() {
        viewModelScope.launch {
            settingsRepository.forceSyncToFirebase()
        }
    }
    
    /**
     * Загрузка настроек из Firebase
     * 
     * Используется при первом запуске или смене устройства
     */
    fun loadSettingsFromCloud() {
        viewModelScope.launch {
            settingsRepository.loadSettingsFromFirebase()
        }
    }
}
