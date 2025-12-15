package com.imdoctor.flotilla.presentation.screens.game.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.audio.AudioManager
import com.imdoctor.flotilla.data.local.preferences.AIStatisticsDataStore
import com.imdoctor.flotilla.data.remote.websocket.models.ShipPlacement
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.presentation.screens.game.models.*
import com.imdoctor.flotilla.utils.Logger
import com.imdoctor.flotilla.utils.ShipPlacementAlgorithm
import com.imdoctor.flotilla.utils.VibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "AIGameViewModel"

/**
 * ViewModel для управления игрой против AI
 *
 * Управляет логикой игры, ходами игрока и AI, состоянием игры
 */
class AIGameViewModel(
    private val difficulty: AIDifficulty,
    private val playerShips: List<ShipPlacement>,
    private val aiStatistics: AIStatisticsDataStore = AppContainer.aiStatisticsDataStore,
    private val audioManager: AudioManager = AppContainer.audioManager,
    private val vibrationManager: VibrationManager = AppContainer.vibrationManager
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _uiState = MutableStateFlow<AIGameUiState>(AIGameUiState.Loading)
    val uiState: StateFlow<AIGameUiState> = _uiState.asStateFlow()

    private val aiOpponent: AIOpponent = when (difficulty) {
        AIDifficulty.EASY -> EasyAI()
        AIDifficulty.HARD -> HardAI()
    }

    // Счётчики для статистики
    private var totalMoves = 0
    private var gameStartTime = 0L

    init {
        initializeGame()
    }

    /**
     * Инициализировать игру
     */
    private fun initializeGame() {
        viewModelScope.launch {
            try {
                Logger.i(TAG, "Initializing AI game (difficulty: $difficulty)")
                _uiState.value = AIGameUiState.Loading

                gameStartTime = System.currentTimeMillis()

                // Создать поле игрока с кораблями
                val playerBoard = createBoardFromShips(playerShips)

                // Создать случайное поле для AI
                val aiShips = ShipPlacementAlgorithm.generateRandomPlacement()
                if (aiShips == null) {
                    _uiState.value = AIGameUiState.Error("Не удалось сгенерировать корабли AI")
                    Logger.e(TAG, "Failed to generate AI ships")
                    return@launch
                }

                val aiBoard = createBoardFromShips(aiShips.map { ship ->
                    ShipPlacement(
                        x = ship.x,
                        y = ship.y,
                        length = ship.length,
                        orientation = when (ship.orientation) {
                            com.imdoctor.flotilla.utils.ShipPlacementValidator.Orientation.HORIZONTAL -> "horizontal"
                            com.imdoctor.flotilla.utils.ShipPlacementValidator.Orientation.VERTICAL -> "vertical"
                        }
                    )
                })

                // Создать пустое поле для атак игрока (не показываем корабли AI)
                val aiVisibleBoard = Board()

                val initialState = GameState(
                    gameId = "ai_${System.currentTimeMillis()}",
                    opponentNickname = when (difficulty) {
                        AIDifficulty.EASY -> "AI (Лёгкий)"
                        AIDifficulty.HARD -> "AI (Сложный)"
                    },
                    myBoard = playerBoard,
                    opponentBoard = aiVisibleBoard,
                    isMyTurn = true,  // Игрок ходит первым
                    gameOver = false,
                    winner = null
                )

                _gameState.value = initialState
                _uiState.value = AIGameUiState.YourTurn

                // Сохранить настоящее поле AI для проверки попаданий
                realAIBoard = aiBoard

                Logger.i(TAG, "AI game initialized. Player goes first.")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to initialize AI game", e)
                _uiState.value = AIGameUiState.Error("Ошибка инициализации: ${e.message}")
            }
        }
    }

    // Настоящее поле AI (скрыто от игрока)
    private var realAIBoard: Board? = null

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
     * Сделать ход игрока (выстрел по полю AI)
     *
     * @param x Координата X (0-9)
     * @param y Координата Y (0-9)
     */
    fun makeMove(x: Int, y: Int) {
        val currentState = _gameState.value ?: return
        val aiBoard = realAIBoard ?: return

        // Проверить, что сейчас ход игрока
        if (!currentState.isMyTurn) {
            Logger.w(TAG, "Attempted to make move but it's not your turn")
            return
        }

        // Проверить, что клетка ещё не была атакована
        val visibleCell = currentState.opponentBoard.getCell(x, y)
        if (visibleCell?.state != CellState.EMPTY && visibleCell?.state != CellState.SHIP) {
            Logger.w(TAG, "Cell already attacked: ($x, $y)")
            return
        }

        viewModelScope.launch {
            try {
                Logger.i(TAG, "Player move: ($x, $y)")
                totalMoves++

                // Проверить результат на настоящем поле AI
                val realCell = aiBoard.getCell(x, y)
                val isHit = realCell?.state == CellState.SHIP

                if (isHit) {
                    // Попадание!
                    audioManager.playHit()
                    vibrationManager.vibrateHit()

                    // Найти корабль
                    val ship = aiBoard.getShipAt(x, y)
                    var updatedAIBoard = aiBoard.updateCell(x, y, CellState.HIT)
                    var updatedVisibleBoard = currentState.opponentBoard.updateCell(x, y, CellState.HIT)

                    if (ship != null) {
                        updatedAIBoard = updatedAIBoard.updateShip(ship.id, Pair(x, y))

                        // Проверить, потоплен ли корабль
                        val updatedShip = updatedAIBoard.ships.find { it.id == ship.id }
                        val isSunk = updatedShip?.isSunk == true

                        if (isSunk) {
                            // Корабль потоплен!
                            Logger.i(TAG, "AI ship sunk: ${ship.id}")

                            updatedShip.positions.forEach { (sx, sy) ->
                                updatedVisibleBoard = updatedVisibleBoard.updateCell(sx, sy, CellState.SUNK)
                            }

                            // Проверить, все ли корабли AI потоплены
                            val allAIShipsSunk = updatedAIBoard.ships.all { it.isSunk }
                            if (allAIShipsSunk) {
                                // Победа игрока!
                                handleGameOver(true, updatedVisibleBoard, currentState.myBoard)
                                return@launch
                            }
                        }
                    }

                    realAIBoard = updatedAIBoard
                    _gameState.value = currentState.copy(
                        opponentBoard = updatedVisibleBoard,
                        isMyTurn = true  // Игрок ходит снова после попадания
                    )
                    _uiState.value = AIGameUiState.YourTurn

                } else {
                    // Промах
                    audioManager.playMiss()

                    val updatedVisibleBoard = currentState.opponentBoard.updateCell(x, y, CellState.MISS)

                    _gameState.value = currentState.copy(
                        opponentBoard = updatedVisibleBoard,
                        isMyTurn = false
                    )
                    _uiState.value = AIGameUiState.AITurn

                    // Ход AI
                    delay(600)  // Небольшая пауза перед ходом AI
                    makeAIMove()
                }

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to make player move", e)
                _uiState.value = AIGameUiState.Error("Ошибка хода: ${e.message}")
            }
        }
    }

    /**
     * Выполнить ход AI (и продолжать ходить при попаданиях)
     */
    private suspend fun makeAIMove() {
        var shouldContinue = true

        while (shouldContinue) {
            // КРИТИЧНО: Читаем свежий state на каждой итерации
            val currentState = _gameState.value ?: return

            if (!currentState.isMyTurn && !currentState.gameOver) {
                try {
                    // AI выбирает цель
                    val (x, y) = aiOpponent.getNextMove(currentState.myBoard)
                    Logger.i(TAG, "AI move: ($x, $y)")

                    delay(400)
                    totalMoves++

                    // Проверить результат
                    val cell = currentState.myBoard.getCell(x, y)
                    val isHit = cell?.state == CellState.SHIP

                    if (isHit) {
                        // AI попал!
                        audioManager.playHit()
                        vibrationManager.vibrateHit()

                        var updatedPlayerBoard = currentState.myBoard.updateCell(x, y, CellState.HIT)
                        val ship = updatedPlayerBoard.getShipAt(x, y)

                        var isSunk = false
                        if (ship != null) {
                            updatedPlayerBoard = updatedPlayerBoard.updateShip(ship.id, Pair(x, y))

                            val updatedShip = updatedPlayerBoard.ships.find { it.id == ship.id }
                            isSunk = updatedShip?.isSunk == true

                            if (isSunk) {
                                Logger.i(TAG, "Player ship sunk by AI: ${ship.id}")

                                updatedShip?.positions?.forEach { (sx, sy) ->
                                    updatedPlayerBoard = updatedPlayerBoard.updateCell(sx, sy, CellState.SUNK)
                                }

                                val allPlayerShipsSunk = updatedPlayerBoard.ships.all { it.isSunk }
                                if (allPlayerShipsSunk) {
                                    handleGameOver(false, currentState.opponentBoard, updatedPlayerBoard)
                                    return
                                }
                            }
                        }

                        aiOpponent.notifyMoveResult(x, y, hit = true, sunk = isSunk)

                        _gameState.value = currentState.copy(
                            myBoard = updatedPlayerBoard,
                            isMyTurn = false
                        )
                        _uiState.value = AIGameUiState.AITurn

                        // AI продолжает - цикл повторится
                        delay(800)
                        shouldContinue = true

                    } else {
                        // AI промахнулся
                        audioManager.playMiss()

                        val updatedPlayerBoard = currentState.myBoard.updateCell(x, y, CellState.MISS)
                        aiOpponent.notifyMoveResult(x, y, hit = false, sunk = false)

                        _gameState.value = currentState.copy(
                            myBoard = updatedPlayerBoard,
                            isMyTurn = true
                        )
                        _uiState.value = AIGameUiState.YourTurn

                        // Ход AI завершён
                        shouldContinue = false
                    }

                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to make AI move", e)
                    _uiState.value = AIGameUiState.Error("Ошибка хода AI: ${e.message}")
                    shouldContinue = false
                }
            } else {
                shouldContinue = false
            }
        }
    }

    /**
     * Обработать завершение игры
     */
    private fun handleGameOver(playerWon: Boolean, aiBoard: Board, playerBoard: Board) {
        viewModelScope.launch {
            try {
                val gameDuration = (System.currentTimeMillis() - gameStartTime) / 1000.0

                Logger.i(TAG, "Game over! Player won: $playerWon, moves: $totalMoves, duration: ${gameDuration}s")

                // Воспроизвести звук
                if (playerWon) {
                    audioManager.playVictory()
                } else {
                    audioManager.playDefeat()
                }

                // Записать статистику
                if (playerWon) {
                    aiStatistics.recordWin(difficulty)
                } else {
                    aiStatistics.recordLoss(difficulty)
                }

                _gameState.value = _gameState.value?.copy(
                    opponentBoard = aiBoard,
                    myBoard = playerBoard,
                    gameOver = true,
                    winner = if (playerWon) "player" else "ai",
                    isMyTurn = false
                )

                _uiState.value = if (playerWon) {
                    AIGameUiState.Victory(
                        totalMoves = totalMoves,
                        durationSeconds = gameDuration
                    )
                } else {
                    AIGameUiState.Defeat(
                        totalMoves = totalMoves,
                        durationSeconds = gameDuration
                    )
                }

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to handle game over", e)
            }
        }
    }

    /**
     * UI состояния для AI игры
     */
    sealed class AIGameUiState {
        /** Загрузка/инициализация */
        object Loading : AIGameUiState()

        /** Ваш ход */
        object YourTurn : AIGameUiState()

        /** Ход AI */
        object AITurn : AIGameUiState()

        /** Победа */
        data class Victory(
            val totalMoves: Int,
            val durationSeconds: Double
        ) : AIGameUiState()

        /** Поражение */
        data class Defeat(
            val totalMoves: Int,
            val durationSeconds: Double
        ) : AIGameUiState()

        /** Ошибка */
        data class Error(val message: String) : AIGameUiState()
    }
}
