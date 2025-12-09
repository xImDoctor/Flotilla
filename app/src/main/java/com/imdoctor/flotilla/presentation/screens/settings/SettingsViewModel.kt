package com.imdoctor.flotilla.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.data.repository.NicknameChangeException
import com.imdoctor.flotilla.data.repository.SettingsRepository
import com.imdoctor.flotilla.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Результат изменения никнейма
 */
sealed class NicknameUpdateResult {
    object Idle : NicknameUpdateResult()
    object Loading : NicknameUpdateResult()
    object Success : NicknameUpdateResult()
    data class Error(val messageResId: Int) : NicknameUpdateResult()
}

/**
 * ViewModel для экрана настроек
 *
 * Управляет состоянием настроек и их сохранением
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Состояние изменения никнейма
    private val _nicknameUpdateResult = MutableStateFlow<NicknameUpdateResult>(NicknameUpdateResult.Idle)
    val nicknameUpdateResult: StateFlow<NicknameUpdateResult> = _nicknameUpdateResult.asStateFlow()

    init {
        // Синхронизируем никнейм из UserProfile ТОЛЬКО если он дефолтный
        syncNicknameIfNeeded()
    }

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
     * Фоновая музыка включена
     */
    val musicEnabled: StateFlow<Boolean> = settingsRepository.musicEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Звуковые эффекты включены
     */
    val soundEffectsEnabled: StateFlow<Boolean> = settingsRepository.soundEffectsEnabledFlow
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
            _nicknameUpdateResult.value = NicknameUpdateResult.Loading

            val result = settingsRepository.setNickname(newNickname)

            _nicknameUpdateResult.value = result.fold(
                onSuccess = { NicknameUpdateResult.Success },
                onFailure = { error ->
                    val messageResId = when (error) {
                        is NicknameChangeException.TimeoutNotExpired ->
                            R.string.settings_nickname_error_timeout
                        is NicknameChangeException.NicknameAlreadyTaken ->
                            R.string.settings_nickname_error_taken
                        is NicknameChangeException.InvalidNickname ->
                            R.string.settings_nickname_error_invalid
                        is NicknameChangeException.NetworkError ->
                            R.string.settings_nickname_error_network
                        else ->
                            R.string.settings_nickname_error_network
                    }
                    NicknameUpdateResult.Error(messageResId)
                }
            )
        }
    }

    /**
     * Сброс состояния изменения никнейма
     *
     * Вызывается после показа сообщения пользователю
     */
    fun clearNicknameUpdateResult() {
        _nicknameUpdateResult.value = NicknameUpdateResult.Idle
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
     * Переключение фоновой музыки
     *
     * @param enabled Включена ли музыка
     */
    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    /**
     * Переключение звуковых эффектов
     *
     * @param enabled Включены ли звуки
     */
    fun toggleSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEffectsEnabled(enabled)
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

    /**
     * Синхронизация никнейма из UserProfile ТОЛЬКО если он дефолтный
     *
     * Вызывается один раз при первом открытии настроек
     */
    private fun syncNicknameIfNeeded() {
        viewModelScope.launch {
            try {
                // Получаем текущий никнейм из настроек
                val currentNickname = nickname.value

                // Синхронизируем только если никнейм дефолтный
                if (currentNickname == "Player" || currentNickname.isBlank()) {
                    val profileResult = userRepository.getUserProfile()
                    profileResult.getOrNull()?.let { profile ->
                        // Если в профиле есть настоящий никнейм, используем его
                        if (profile.nickname.isNotBlank() && profile.nickname != "Player") {
                            settingsRepository.setNickname(profile.nickname)
                        }
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибки синхронизации
            }
        }
    }
}
