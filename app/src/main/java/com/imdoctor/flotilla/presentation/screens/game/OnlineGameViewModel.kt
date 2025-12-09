package com.imdoctor.flotilla.presentation.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.audio.AudioManager
import com.imdoctor.flotilla.data.remote.websocket.WebSocketManager
import com.imdoctor.flotilla.data.remote.websocket.models.*
import com.imdoctor.flotilla.data.repository.UserRepository
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.presentation.screens.game.models.*
import com.imdoctor.flotilla.utils.Logger
import com.imdoctor.flotilla.utils.VibrationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.decodeFromJsonElement

private const val TAG = "OnlineGameViewModel"

/**
 * ViewModel для управления онлайн игрой
 *
 * Управляет WebSocket соединением с игрой, обработкой ходов и состоянием игры
 */
class OnlineGameViewModel(
    private val gameId: String,
    private val opponentNickname: String,
    private val yourTurn: Boolean,
    private val myShips: List<ShipPlacement>,
    private val webSocketManager: WebSocketManager,
    private val userRepository: UserRepository,
    private val audioManager: AudioManager = AppContainer.audioManager,
    private val vibrationManager: VibrationManager = AppContainer.vibrationManager
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Кэш для ID текущего пользователя (для определения победителя)
    private var currentUserId: String? = null

    init {
        connectToGame()
    }

    /**
     * Подключиться к игровому WebSocket
     */
    private fun connectToGame() {
        viewModelScope.launch {
            try {
                Logger.i(TAG, "Connecting to game: $gameId")
                _uiState.value = GameUiState.Loading

                // Получить Firebase токен
                val token = userRepository.getCurrentUserToken()
                if (token == null) {
                    _uiState.value = GameUiState.Error("Не удалось получить токен аутентификации")
                    return@launch
                }

                // Получить ID текущего пользователя
                val profile = userRepository.getCurrentUserProfile()
                currentUserId = profile?.userId

                // Инициализировать игровое состояние
                initializeGameState()

                // Подключиться к игровому WebSocket
                webSocketManager.connectToGame(gameId, token)
                    .collect { event ->
                        handleGameEvent(event)
                    }

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to connect to game", e)
                _uiState.value = GameUiState.Error(
                    e.message ?: "Неизвестная ошибка при подключении к игре"
                )
            }
        }
    }

    /**
     * Инициализировать начальное состояние игры
     */
    private fun initializeGameState() {
        // Создать моё игровое поле с кораблями
        val myBoard = createBoardFromShips(myShips)

        // Создать пустое поле противника (для атак)
        val opponentBoard = Board()

        val initialState = GameState(
            gameId = gameId,
            opponentNickname = opponentNickname,
            myBoard = myBoard,
            opponentBoard = opponentBoard,
            isMyTurn = yourTurn,
            gameOver = false,
            winner = null
        )

        _gameState.value = initialState
        _uiState.value = if (yourTurn) {
            GameUiState.YourTurn
        } else {
            GameUiState.OpponentTurn
        }

        Logger.i(TAG, "Game initialized. Your turn: $yourTurn")
    }

    /**
     * Создать игровое поле из расстановки кораблей
     */
    private fun createBoardFromShips(shipPlacements: List<ShipPlacement>): Board {
        val ships = mutableListOf<Ship>()
        val cells = Array(10) { x ->
            Array(10) { y ->
                Cell(x, y, CellState.EMPTY)
            }
        }

        // Создать корабли и обновить клетки
        shipPlacements.forEachIndexed { index, placement ->
            val positions = mutableListOf<Pair<Int, Int>>()

            // Вычислить все позиции корабля
            for (i in 0 until placement.length) {
                val (x, y) = if (placement.orientation == "horizontal") {
                    Pair(placement.x + i, placement.y)
                } else {
                    Pair(placement.x, placement.y + i)
                }

                positions.add(Pair(x, y))

                // Обновить клетку
                if (x in 0..9 && y in 0..9) {
                    cells[x][y] = Cell(x, y, CellState.SHIP)
                }
            }

            // Создать корабль
            val ship = Ship(
                id = "ship_$index",
                length = placement.length,
                positions = positions
            )
            ships.add(ship)
        }

        return Board(cells = cells, ships = ships)
    }

    /**
     * Сделать ход (выстрелить по полю противника)
     *
     * @param x Координата X (0-9)
     * @param y Координата Y (0-9)
     */
    fun makeMove(x: Int, y: Int) {
        val currentState = _gameState.value ?: return

        // Проверить, что сейчас наш ход
        if (!currentState.isMyTurn) {
            Logger.w(TAG, "Attempted to make move but it's not your turn")
            return
        }

        // Проверить, что клетка ещё не была атакована
        val cell = currentState.opponentBoard.getCell(x, y)
        if (cell?.state != CellState.EMPTY && cell?.state != CellState.SHIP) {
            Logger.w(TAG, "Cell already attacked: ($x, $y)")
            return
        }

        viewModelScope.launch {
            try {
                Logger.i(TAG, "Making move: ($x, $y)")

                val moveData = MoveData(x = x, y = y)
                val dataJson = json.encodeToJsonElement(moveData)
                val event = WSEvent(
                    type = WSEventType.MAKE_MOVE,
                    data = dataJson
                )

                webSocketManager.sendToGame(event)

                // UI состояние: ждём результата
                _uiState.value = GameUiState.WaitingForResult

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to make move", e)
                _uiState.value = GameUiState.Error("Не удалось сделать ход: ${e.message}")
            }
        }
    }

    /**
     * Обработать событие от WebSocket сервера
     */
    private fun handleGameEvent(event: WSEvent) {
        Logger.d(TAG, "Received event: ${event.type}")

        when (event.type) {
            WSEventType.MOVE_RESULT -> handleMoveResult(event)
            WSEventType.OPPONENT_MOVE -> handleOpponentMove(event)
            WSEventType.YOUR_TURN -> handleYourTurn(event)
            WSEventType.GAME_OVER -> handleGameOver(event)
            WSEventType.OPPONENT_DISCONNECTED -> handleOpponentDisconnected()
            WSEventType.OPPONENT_RECONNECTED -> handleOpponentReconnected()
            WSEventType.PING -> handlePing()
            WSEventType.ERROR -> handleError(event)
            else -> Logger.w(TAG, "Unknown event type: ${event.type}")
        }
    }

    /**
     * Обработать результат нашего хода
     */
    private fun handleMoveResult(event: WSEvent) {
        try {
            val data = json.decodeFromJsonElement<MoveResultData>(event.data!!)
            val currentState = _gameState.value ?: return

            Logger.i(TAG, "Move result: (${data.x}, ${data.y}) = ${data.result}")

            // Воспроизвести звук и вибрацию в зависимости от результата
            when (data.result) {
                MoveResult.HIT, MoveResult.SUNK -> {
                    audioManager.playHit()
                    vibrationManager.vibrateHit()
                }
                MoveResult.MISS -> {
                    audioManager.playMiss()
                }
                MoveResult.WIN -> {
                    audioManager.playVictory()
                }
                else -> { /* no sound for other results */ }
            }

            // Обновить поле противника
            val newOpponentBoard = when (data.result) {
                MoveResult.MISS -> {
                    currentState.opponentBoard.updateCell(data.x, data.y, CellState.MISS)
                }
                MoveResult.HIT -> {
                    var board = currentState.opponentBoard.updateCell(data.x, data.y, CellState.HIT)
                    // Если есть shipId, обновить корабль
                    if (data.shipId != null) {
                        board = board.updateShip(data.shipId, Pair(data.x, data.y))
                    }
                    board
                }
                MoveResult.SUNK -> {
                    var board = currentState.opponentBoard.updateCell(data.x, data.y, CellState.SUNK)
                    // Обновить корабль как потопленный
                    if (data.shipId != null) {
                        board = board.updateShip(data.shipId, Pair(data.x, data.y))
                        // Отметить все клетки корабля как SUNK
                        val ship = board.ships.find { it.id == data.shipId }
                        ship?.positions?.forEach { (x, y) ->
                            board = board.updateCell(x, y, CellState.SUNK)
                        }
                    }
                    board
                }
                MoveResult.WIN -> {
                    // Победа! Обработаем в game_over событии
                    currentState.opponentBoard.updateCell(data.x, data.y, CellState.SUNK)
                }
                else -> currentState.opponentBoard
            }

            _gameState.value = currentState.copy(opponentBoard = newOpponentBoard)

            // Если игра не закончена, ждём хода противника
            if (!data.gameOver) {
                _uiState.value = GameUiState.OpponentTurn
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse move_result", e)
        }
    }

    /**
     * Обработать ход противника
     */
    private fun handleOpponentMove(event: WSEvent) {
        try {
            val data = json.decodeFromJsonElement<OpponentMoveData>(event.data!!)
            val currentState = _gameState.value ?: return

            Logger.i(TAG, "Opponent move: (${data.x}, ${data.y}) = ${data.result}")

            // Воспроизвести звук и вибрацию в зависимости от результата
            when (data.result) {
                "hit" -> {
                    audioManager.playHit()
                    vibrationManager.vibrateHit()
                }
                "miss" -> {
                    audioManager.playMiss()
                }
            }

            // Обновить моё поле
            val newMyBoard = when (data.result) {
                "miss" -> {
                    currentState.myBoard.updateCell(data.x, data.y, CellState.MISS)
                }
                "hit" -> {
                    var board = currentState.myBoard.updateCell(data.x, data.y, CellState.HIT)
                    // Обновить корабль
                    val ship = board.getShipAt(data.x, data.y)
                    if (ship != null) {
                        board = board.updateShip(ship.id, Pair(data.x, data.y))

                        // Проверить, потоплен ли корабль
                        val updatedShip = board.ships.find { it.id == ship.id }
                        if (updatedShip?.isSunk == true) {
                            // Отметить все клетки как SUNK
                            updatedShip.positions.forEach { (x, y) ->
                                board = board.updateCell(x, y, CellState.SUNK)
                            }
                        }
                    }
                    board
                }
                else -> currentState.myBoard
            }

            _gameState.value = currentState.copy(myBoard = newMyBoard)

        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse opponent_move", e)
        }
    }

    /**
     * Обработать начало нашего хода
     */
    private fun handleYourTurn(event: WSEvent) {
        val currentState = _gameState.value ?: return
        _gameState.value = currentState.copy(isMyTurn = true)
        _uiState.value = GameUiState.YourTurn
        Logger.i(TAG, "It's your turn")
    }

    /**
     * Обработать завершение игры
     */
    private fun handleGameOver(event: WSEvent) {
        try {
            val data = json.decodeFromJsonElement<GameOverData>(event.data!!)
            val currentState = _gameState.value ?: return

            Logger.i(TAG, "Game over! Winner: ${data.winnerId}")

            // Определить победителя
            val isWinner = data.winnerId == currentUserId

            // Воспроизвести звук победы или поражения
            if (isWinner) {
                audioManager.playVictory()
            } else {
                audioManager.playDefeat()
            }

            _gameState.value = currentState.copy(
                gameOver = true,
                winner = data.winnerId,
                isMyTurn = false
            )

            _uiState.value = if (isWinner) {
                GameUiState.Victory(
                    totalMoves = data.totalMoves,
                    durationSeconds = data.durationSeconds
                )
            } else {
                GameUiState.Defeat(
                    totalMoves = data.totalMoves,
                    durationSeconds = data.durationSeconds
                )
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse game_over", e)
        }
    }

    /**
     * Обработать отключение противника
     */
    private fun handleOpponentDisconnected() {
        Logger.w(TAG, "Opponent disconnected")
        _uiState.value = GameUiState.OpponentDisconnected
    }

    /**
     * Обработать переподключение противника
     */
    private fun handleOpponentReconnected() {
        Logger.i(TAG, "Opponent reconnected")
        val currentState = _gameState.value ?: return
        _uiState.value = if (currentState.isMyTurn) {
            GameUiState.YourTurn
        } else {
            GameUiState.OpponentTurn
        }
    }

    /**
     * Обработать ping от сервера
     */
    private fun handlePing() {
        val pongEvent = WSEvent(type = WSEventType.PONG)
        webSocketManager.sendToGame(pongEvent)
    }

    /**
     * Обработать ошибку от сервера
     */
    private fun handleError(event: WSEvent) {
        val errorMsg = event.error ?: "Неизвестная ошибка сервера"
        Logger.e(TAG, "Server error: $errorMsg")
        _uiState.value = GameUiState.Error(errorMsg)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnectGame()
    }

    /**
     * UI состояния для игры
     */
    sealed class GameUiState {
        /** Загрузка/подключение */
        object Loading : GameUiState()

        /** Ваш ход */
        object YourTurn : GameUiState()

        /** Ход противника */
        object OpponentTurn : GameUiState()

        /** Ожидание результата хода */
        object WaitingForResult : GameUiState()

        /** Противник отключился */
        object OpponentDisconnected : GameUiState()

        /** Победа */
        data class Victory(
            val totalMoves: Int,
            val durationSeconds: Double
        ) : GameUiState()

        /** Поражение */
        data class Defeat(
            val totalMoves: Int,
            val durationSeconds: Double
        ) : GameUiState()

        /** Ошибка */
        data class Error(val message: String) : GameUiState()
    }
}
