package com.imdoctor.flotilla.presentation.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.remote.firebase.FirebaseAuthManager
import com.imdoctor.flotilla.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана регистрации пользователя
 *
 * Управляет состоянием ввода никнейма и процессом создания профиля
 */
class UserRegistrationViewModel(
    private val userRepository: UserRepository,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    // Состояние никнейма
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    // Состояние процесса регистрации
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Состояние успешной регистрации
    private val _registrationComplete = MutableStateFlow(false)
    val registrationComplete: StateFlow<Boolean> = _registrationComplete.asStateFlow()

    /**
     * Обновление никнейма
     */
    fun updateNickname(newNickname: String) {
        // Ограничение длины никнейма и очистка недопустимых символов
        val sanitized = newNickname
            .filter { it.isLetterOrDigit() || it.isWhitespace() || it == '_' || it == '-' }
            .take(20)
        _nickname.value = sanitized

        // Сброс ошибки при изменении никнейма
        _errorMessage.value = null
    }

    /**
     * Валидация никнейма
     */
    private fun isNicknameValid(): Boolean {
        val trimmedNickname = _nickname.value.trim()
        return trimmedNickname.isNotEmpty() && trimmedNickname.length >= 3
    }

    /**
     * Создание профиля пользователя
     */
    fun completeRegistration() {
        if (!isNicknameValid()) {
            _errorMessage.value = "Никнейм должен содержать минимум 3 символа"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Сначала аутентифицируем пользователя анонимно
                val authResult = authManager.signInAnonymously()

                authResult.fold(
                    onSuccess = { firebaseUser ->
                        // После успешной аутентификации создаём профиль
                        val profileResult = userRepository.createUserProfile(_nickname.value.trim())

                        profileResult.fold(
                            onSuccess = {
                                _registrationComplete.value = true
                            },
                            onFailure = { exception ->
                                _errorMessage.value = exception.message ?: "Ошибка при создании профиля"
                            }
                        )
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Ошибка аутентификации: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Сброс состояния ошибки
     */
    fun clearError() {
        _errorMessage.value = null
    }
}