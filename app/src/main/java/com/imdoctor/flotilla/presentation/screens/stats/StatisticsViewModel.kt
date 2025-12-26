package com.imdoctor.flotilla.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.local.preferences.AIStatisticsDataStore
import com.imdoctor.flotilla.data.remote.firebase.models.GameHistory
import com.imdoctor.flotilla.data.remote.firebase.models.UserProfile
import com.imdoctor.flotilla.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана статистики
 *
 * Управляет загрузкой и отображением:
 * - Статистики пользователя (побед, поражений, точности)
 * - Истории игр
 * - AI статистики (локальной)
 */
class StatisticsViewModel(
    private val userRepository: UserRepository,
    private val aiStatistics: AIStatisticsDataStore
) : ViewModel() {


    // STATE для UI

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _gameHistory = MutableStateFlow<List<GameHistory>>(emptyList())
    val gameHistory: StateFlow<List<GameHistory>> = _gameHistory.asStateFlow()

    // AI статистика (локальная)
    val easyWins: StateFlow<Int> = aiStatistics.easyWinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val easyLosses: StateFlow<Int> = aiStatistics.easyLossesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mediumWins: StateFlow<Int> = aiStatistics.mediumWinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mediumLosses: StateFlow<Int> = aiStatistics.mediumLossesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hardWins: StateFlow<Int> = aiStatistics.hardWinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hardLosses: StateFlow<Int> = aiStatistics.hardLossesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
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
