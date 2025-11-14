package com.imdoctor.flotilla.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.repository.UserRepository
import com.imdoctor.flotilla.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для определения стартового экрана приложения
 *
 * Проверяет наличие профиля пользователя и определяет,
 * показывать экран регистрации или главное меню
 */
class StartupViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        checkUserProfile()
    }

    /**
     * Проверка наличия профиля пользователя
     */
    private fun checkUserProfile() {
        viewModelScope.launch {
            try {
                val hasProfile = userRepository.hasProfile()

                _startDestination.value = if (hasProfile) {
                    Screen.MainMenu.route
                } else {
                    Screen.UserRegistration.route
                }
            } catch (e: Exception) {
                // В случае ошибки показываем экран регистрации
                _startDestination.value = Screen.UserRegistration.route
            }
        }
    }
}
