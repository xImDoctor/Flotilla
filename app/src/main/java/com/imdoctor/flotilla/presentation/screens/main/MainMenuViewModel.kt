package com.imdoctor.flotilla.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.repository.SettingsRepository
import com.imdoctor.flotilla.utils.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainMenuViewModel"

/**
 * ViewModel для главного меню
 *
 * Управляет:
 * - Текущим выбранным языком
 * - Сменой языка с сохранением в DataStore
 * - События для пересоздания Activity
 */
class MainMenuViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * StateFlow с текущим языком
     */
    val currentLanguage: StateFlow<String> = settingsRepository.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "en"
        )

    /**
     * SharedFlow для события смены языка (для пересоздания Activity)
     */
    private val _languageChangeEvent = MutableSharedFlow<String>()
    val languageChangeEvent: SharedFlow<String> = _languageChangeEvent.asSharedFlow()

    /**
     * Смена языка
     *
     * @param languageCode Код языка ("en" или "ru")
     */
    fun changeLanguage(languageCode: String) {
        Logger.d(TAG, "changeLanguage() called with: $languageCode")
        viewModelScope.launch {
            // Сохраняем в DataStore
            settingsRepository.setLanguage(languageCode)
            Logger.d(TAG, "Language saved to DataStore: $languageCode")

            // Отправляем событие с кодом языка для пересоздания Activity
            _languageChangeEvent.emit(languageCode)
            Logger.d(TAG, "Language change event emitted: $languageCode")
        }
    }
}
