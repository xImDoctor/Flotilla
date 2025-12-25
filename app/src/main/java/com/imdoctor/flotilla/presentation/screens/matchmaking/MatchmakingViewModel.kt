package com.imdoctor.flotilla.presentation.screens.matchmaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.remote.websocket.WebSocketManager
import com.imdoctor.flotilla.data.remote.websocket.models.*
import com.imdoctor.flotilla.data.repository.MatchmakingDataHolder
import com.imdoctor.flotilla.data.repository.UserRepository
import com.imdoctor.flotilla.utils.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.decodeFromJsonElement

private const val TAG = "MatchmakingViewModel"

/**
 * ViewModel для управления matchmaking процессом
 *
 * Управляет WebSocket соединением, поиском соперника и навигацией к игре
 */
class MatchmakingViewModel(
    private val webSocketManager: WebSocketManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchmakingUiState>(MatchmakingUiState.Idle)
    val uiState: StateFlow<MatchmakingUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Кэш кораблей, отправленных на matchmaking
    private var sentShips: List<ShipPlacement> = emptyList()

    /**
     * Начать поиск матча
     *
     * Подключается к WebSocket и присоединяется к очереди
     *
     * @param ships Список расстановки кораблей (пока заглушка)
     */
    fun startMatchmaking(ships: List<ShipPlacement> = emptyList()) {
        viewModelScope.launch {
            try {
                _uiState.value = MatchmakingUiState.Connecting
                Logger.i(TAG, "Starting matchmaking")

                // Получить Firebase токен
                val token = userRepository.getCurrentUserToken()
                if (token == null) {
                    _uiState.value = MatchmakingUiState.Error("Не удалось получить токен аутентификации")
                    return@launch
                }

                // Получить никнейм пользователя
                val profile = userRepository.getCurrentUserProfile()
                val nickname = profile?.nickname ?: "Player"

                // Подготовить корабли: сначала из DataHolder, потом из параметра, или mock
                val shipPlacements = MatchmakingDataHolder.getPreparedShips()
                    ?: if (ships.isNotEmpty()) ships else createMockShips()

                sentShips = shipPlacements

                Logger.i(TAG, "Using ships for matchmaking: ${shipPlacements.size} ships")

                // Подключиться к matchmaking WebSocket с callback
                webSocketManager.connectToMatchmaking(
                    token = token,
                    onConnected = {
                        // ВАЖНО: Callback вызывается в OkHttp thread, нужен coroutine scope
                        viewModelScope.launch {
                            // Сразу после подключения отправляем join_queue
                            Logger.i(TAG, ">>> [CALLBACK] WebSocket connected! Preparing join_queue...")
                            try {
                                val data = JoinQueueData(
                                    nickname = nickname,
                                    ships = shipPlacements
                                )
                                val dataJson = json.encodeToJsonElement(data)
                                val event = WSEvent(
                                    type = WSEventType.JOIN_QUEUE,
                                    data = dataJson
                                )
                                Logger.i(TAG, ">>> [CALLBACK] Sending join_queue for $nickname...")
                                webSocketManager.sendToMatchmaking(event)
                                Logger.i(TAG, ">>> [CALLBACK] join_queue sent!")
                            } catch (e: Exception) {
                                Logger.e(TAG, ">>> [CALLBACK] Failed to send join_queue", e)
                            }
                        }
                    }
                ).collect { event ->
                    handleMatchmakingEvent(event)
                }

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to start matchmaking", e)
                _uiState.value = MatchmakingUiState.Error(
                    e.message ?: "Неизвестная ошибка при подключении"
                )
            }
        }
    }

    /**
     * Присоединиться к очереди matchmaking
     *
     * Отправляет событие join_queue на сервер
     *
     * @param ships Расстановка кораблей (пока заглушка - пустой список)
     */
    fun joinQueue(ships: List<ShipPlacement> = emptyList()) {
        viewModelScope.launch {
            try {
                val profile = userRepository.getCurrentUserProfile()
                val nickname = profile?.nickname ?: "Player"

                // Временная заглушка: создаем тестовые корабли если список пустой
                val shipPlacements = if (ships.isEmpty()) {
                    createMockShips()
                } else {
                    ships
                }

                // Сохраняем корабли для последующей передачи в GameScreen
                sentShips = shipPlacements

                val data = JoinQueueData(
                    nickname = nickname,
                    ships = shipPlacements
                )

                // Создаем JSON элемент для data
                val dataJson = json.encodeToJsonElement(data)

                val event = WSEvent(
                    type = WSEventType.JOIN_QUEUE,
                    data = dataJson
                )

                Logger.i(TAG, "Joining queue with nickname: $nickname")
                webSocketManager.sendToMatchmaking(event)

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to join queue", e)
                _uiState.value = MatchmakingUiState.Error("Не удалось присоединиться к очереди: ${e.message}")
            }
        }
    }

    /**
     * Отменить поиск матча
     *
     * Отправляет событие leave_queue и отключается от WebSocket
     */
    fun cancelMatchmaking() {
        Logger.i(TAG, "Cancelling matchmaking")

        viewModelScope.launch {
            try {
                val event = WSEvent(type = WSEventType.LEAVE_QUEUE)
                webSocketManager.sendToMatchmaking(event)
            } catch (e: Exception) {
                Logger.e(TAG, "Error sending leave_queue", e)
            } finally {
                webSocketManager.disconnectMatchmaking()
                _uiState.value = MatchmakingUiState.Idle
            }
        }
    }

    /**
     * Обработать событие от WebSocket сервера
     *
     * @param event WebSocket событие
     */
    private fun handleMatchmakingEvent(event: WSEvent) {
        Logger.i(TAG, ">>> Received event: ${event.type}, data: ${event.data}")

        when (event.type) {
            WSEventType.WAITING -> {
                try {
                    val data = json.decodeFromJsonElement<WaitingData>(event.data!!)
                    Logger.i(TAG, "Waiting in queue, position: ${data.position}")
                    _uiState.value = MatchmakingUiState.Waiting(
                        position = data.position,
                        queueSize = data.queueSize
                    )
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to parse waiting data", e)
                }
            }

            WSEventType.MATCH_FOUND -> {
                try {
                    val data = json.decodeFromJsonElement<MatchFoundData>(event.data!!)
                    Logger.i(TAG, "Match found! Game ID: ${data.gameId}")

                    // Сохраняем данные matchmaking для GameScreen
                    MatchmakingDataHolder.saveMatchData(
                        gameId = data.gameId,
                        opponentNickname = data.opponentNickname,
                        yourTurn = data.yourTurn,
                        myShips = sentShips
                    )

                    _uiState.value = MatchmakingUiState.MatchFound(
                        gameId = data.gameId,
                        opponentNickname = data.opponentNickname,
                        yourTurn = data.yourTurn
                    )
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to parse match_found data", e)
                }
            }

            WSEventType.ERROR -> {
                val errorMsg = event.error ?: "Неизвестная ошибка сервера"
                Logger.e(TAG, "Server error: $errorMsg")
                _uiState.value = MatchmakingUiState.Error(errorMsg)
            }

            WSEventType.PING -> {
                // Respond to ping with pong
                val pongEvent = WSEvent(type = WSEventType.PONG)
                webSocketManager.sendToMatchmaking(pongEvent)
            }

            else -> {
                Logger.w(TAG, "Unknown event type: ${event.type}")
            }
        }
    }

    /**
     * Создать тестовую расстановку кораблей (заглушка)
     *
     * TODO: Заменить на реальную расстановку из ShipSetupScreen
     */
    private fun createMockShips(): List<ShipPlacement> {
        return listOf(
            // 1 линкор (4 клетки)
            ShipPlacement(x = 0, y = 0, length = 4, orientation = "horizontal"),
            // 2 крейсера (3 клетки)
            ShipPlacement(x = 0, y = 2, length = 3, orientation = "horizontal"),
            ShipPlacement(x = 5, y = 0, length = 3, orientation = "vertical"),
            // 3 эсминца (2 клетки)
            ShipPlacement(x = 0, y = 4, length = 2, orientation = "horizontal"),
            ShipPlacement(x = 3, y = 4, length = 2, orientation = "horizontal"),
            ShipPlacement(x = 6, y = 4, length = 2, orientation = "horizontal"),
            // 4 катера (1 клетка)
            ShipPlacement(x = 0, y = 6, length = 1, orientation = "horizontal"),
            ShipPlacement(x = 2, y = 6, length = 1, orientation = "horizontal"),
            ShipPlacement(x = 4, y = 6, length = 1, orientation = "horizontal"),
            ShipPlacement(x = 6, y = 6, length = 1, orientation = "horizontal")
        )
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnectMatchmaking()
    }

    /**
     * UI состояния для matchmaking
     */
    sealed class MatchmakingUiState {
        /** Начальное состояние */
        object Idle : MatchmakingUiState()

        /** Подключение к серверу */
        object Connecting : MatchmakingUiState()

        /** Ожидание в очереди */
        data class Waiting(
            val position: Int,
            val queueSize: Int? = null
        ) : MatchmakingUiState()

        /** Матч найден */
        data class MatchFound(
            val gameId: String,
            val opponentNickname: String,
            val yourTurn: Boolean
        ) : MatchmakingUiState()

        /** Ошибка */
        data class Error(val message: String) : MatchmakingUiState()
    }
}
