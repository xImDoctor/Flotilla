package com.imdoctor.flotilla.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.remote.firebase.models.GameHistory
import com.imdoctor.flotilla.data.remote.firebase.models.UserProfile
import com.imdoctor.flotilla.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана статистики
 * 
 * Управляет загрузкой и отображением:
 * - Статистики пользователя (побед, поражений, точности)
 * - Истории игр
 */
class StatisticsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    

    // STATE для UI
    
    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    private val _gameHistory = MutableStateFlow<List<GameHistory>>(emptyList())
    val gameHistory: StateFlow<List<GameHistory>> = _gameHistory.asStateFlow()
    
    init {
        loadStatistics()
        loadGameHistory()
    }
    

    // ЗАГРУЗКА ДАННЫХ

    /**
     * Загрузка статистики пользователя
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            
            val result = userRepository.getUserProfile()
            
            result.fold(
                onSuccess = { profile ->
                    _uiState.value = if (profile != null) {
                        StatisticsUiState.Success(profile)
                    } else {
                        StatisticsUiState.Empty
                    }
                },
                onFailure = { error ->
                    _uiState.value = StatisticsUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }
    
    /**
     * Загрузка истории игр
     * 
     * @param limit Максимальное количество игр
     */
    fun loadGameHistory(limit: Long = 50) {
        viewModelScope.launch {
            val result = userRepository.getGameHistory(limit)
            
            result.fold(
                onSuccess = { history ->
                    _gameHistory.value = history
                },
                onFailure = { error ->
                    // Можно добавить обработку ошибок
                    _gameHistory.value = emptyList()
                }
            )
        }
    }
    
    /**
     * Запуск реалтайм отслеживания статистики
     */
    fun startObservingStatistics() {
        viewModelScope.launch {
            userRepository.observeUserProfile()?.collect { profile ->
                _uiState.value = if (profile != null) {
                    StatisticsUiState.Success(profile)
                } else {
                    StatisticsUiState.Empty
                }
            }
        }
    }
    
    /**
     * Запуск реалтайм отслеживания истории игр
     */
    fun startObservingGameHistory(limit: Long = 50) {
        viewModelScope.launch {
            userRepository.observeGameHistory(limit)?.collect { history ->
                _gameHistory.value = history
            }
        }
    }
}

/**
 * UI State для экрана статистики
 */
sealed class StatisticsUiState {
    /**
     * Загрузка данных
     */
    object Loading : StatisticsUiState()
    
    /**
     * Данные успешно загружены
     * 
     * @param profile Профиль пользователя со статистикой
     */
    data class Success(val profile: UserProfile) : StatisticsUiState()
    
    /**
     * Нет данных (новый пользователь)
     */
    object Empty : StatisticsUiState()
    
    /**
     * Ошибка загрузки
     * 
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : StatisticsUiState()
}
