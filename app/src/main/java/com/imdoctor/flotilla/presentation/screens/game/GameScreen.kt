package com.imdoctor.flotilla.presentation.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.data.repository.MatchmakingDataHolder
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.utils.security.SecureScreen

/**
 * Экран онлайн игры
 *
 * @param gameId Идентификатор игры
 * @param onGameEnd Callback при завершении игры
 * @param onExitGame Callback при выходе из игры
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onGameEnd: () -> Unit,
    onExitGame: () -> Unit
) {
    // Защита от скриншотов (для PvP режима)
    SecureScreen()

    // Получить данные matchmaking
    val matchData = MatchmakingDataHolder.getMatchData(gameId)

    if (matchData == null) {
        // Если данных нет, показать ошибку
        ErrorScreen(
            message = "Не удалось загрузить данные игры",
            onBack = onExitGame
        )
        return
    }

    // Создать ViewModel с данными matchmaking
    val viewModel: OnlineGameViewModel = viewModel(
        factory = OnlineGameViewModelFactory(
            gameId = gameId,
            webSocketManager = AppContainer.webSocketManager,
            userRepository = AppContainer.userRepository
        ),
        key = gameId
    )

    val gameState by viewModel.gameState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Обработка завершения игры
    var showVictoryDialog by remember { mutableStateOf(false) }
    var showDefeatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is OnlineGameViewModel.GameUiState.Victory -> {
                showVictoryDialog = true
            }
            is OnlineGameViewModel.GameUiState.Defeat -> {
                showDefeatDialog = true
            }
            else -> {}
        }
    }

    // Диалоги победы/поражения
    if (showVictoryDialog && uiState is OnlineGameViewModel.GameUiState.Victory) {
        val victoryState = uiState as OnlineGameViewModel.GameUiState.Victory
        VictoryDialog(
            totalMoves = victoryState.totalMoves,
            durationSeconds = victoryState.durationSeconds,
            onDismiss = {
                showVictoryDialog = false
                onGameEnd()
            }
        )
    }

    if (showDefeatDialog && uiState is OnlineGameViewModel.GameUiState.Defeat) {
        val defeatState = uiState as OnlineGameViewModel.GameUiState.Defeat
        DefeatDialog(
            totalMoves = defeatState.totalMoves,
            durationSeconds = defeatState.durationSeconds,
            onDismiss = {
                showDefeatDialog = false
                onGameEnd()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_title)) },
                actions = {
                    TextButton(onClick = onExitGame) {
                        Text(stringResource(R.string.common_exit))
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is OnlineGameViewModel.GameUiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }

            is OnlineGameViewModel.GameUiState.Error -> {
                val error = uiState as OnlineGameViewModel.GameUiState.Error
                ErrorScreen(
                    message = error.message,
                    onBack = onExitGame,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                // Игра идёт
                gameState?.let { state ->
                    GameContent(
                        gameState = state,
                        uiState = uiState,
                        onCellClick = { x, y -> viewModel.makeMove(x, y) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

/**
 * Основной контент игры
 */
@Composable
private fun GameContent(
    gameState: com.imdoctor.flotilla.presentation.screens.game.models.GameState,
    uiState: OnlineGameViewModel.GameUiState,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Информация об игре
        GameInfo(
            opponentNickname = gameState.opponentNickname,
            isYourTurn = gameState.isMyTurn
        )

        // Статус игры
        when (uiState) {
            is OnlineGameViewModel.GameUiState.YourTurn -> {
                Text(
                    text = "Ваш ход - выберите клетку для атаки",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is OnlineGameViewModel.GameUiState.OpponentTurn -> {
                Text(
                    text = "Ход противника...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is OnlineGameViewModel.GameUiState.WaitingForResult -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            is OnlineGameViewModel.GameUiState.OpponentDisconnected -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Противник отключился",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            else -> {}
        }

        // Поле противника (для атак)
        Text(
            text = "Поле противника",
            style = MaterialTheme.typography.titleMedium
        )
        GameBoardGrid(
            board = gameState.opponentBoard,
            isInteractive = gameState.isMyTurn && uiState is OnlineGameViewModel.GameUiState.YourTurn,
            onCellClick = onCellClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Моё поле (с кораблями)
        Text(
            text = "Ваше поле",
            style = MaterialTheme.typography.titleMedium
        )
        GameBoardGrid(
            board = gameState.myBoard,
            isInteractive = false, // Нельзя кликать на своё поле
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Экран загрузки
 */
@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Подключение к игре...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Экран ошибки
 */
@Composable
private fun ErrorScreen(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ошибка",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onBack) {
                Text("Вернуться")
            }
        }
    }
}
