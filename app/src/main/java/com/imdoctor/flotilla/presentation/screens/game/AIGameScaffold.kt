package com.imdoctor.flotilla.presentation.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.presentation.screens.game.ai.AIGameViewModel
import com.imdoctor.flotilla.presentation.screens.game.models.GameState

/**
 * Scaffold для AI игры
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIGameScaffold(
    gameState: GameState?,
    uiState: AIGameViewModel.AIGameUiState,
    onCellClick: (Int, Int) -> Unit,
    onExitGame: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    // Перехват системной кнопки "Назад"
    BackHandler(enabled = true) {
        if (gameState?.gameOver == false) {
            showExitDialog = true  // Показать диалог
        } else {
            onExitGame()  // Выйти сразу если игра завершена
        }
    }

    // Диалог подтверждения выхода
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.game_exit_dialog_title)) },
            text = { Text(stringResource(R.string.game_exit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExitGame()
                }) {
                    Text(stringResource(R.string.game_exit_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.game_exit_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_title)) },
                actions = {
                    TextButton(onClick = {
                        // Показываем диалог только если игра не завершена
                        if (gameState?.gameOver == false) {
                            showExitDialog = true
                        } else {
                            onExitGame()
                        }
                    }) {
                        Text(stringResource(R.string.common_exit))
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is AIGameViewModel.AIGameUiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }

            is AIGameViewModel.AIGameUiState.Error -> {
                val error = uiState as AIGameViewModel.AIGameUiState.Error
                ErrorScreen(
                    message = error.message,
                    onBack = onExitGame,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                // Игра идёт
                gameState?.let { state ->
                    AIGameContent(
                        gameState = state,
                        uiState = uiState,
                        onCellClick = onCellClick,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

/**
 * Контент AI игры
 */
@Composable
private fun AIGameContent(
    gameState: GameState,
    uiState: AIGameViewModel.AIGameUiState,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GameInfo с opponent info
        val playerNick = "Player"  // TODO: Получить из UserRepository
        val opponentNick = gameState.opponentNickname

        // Преобразовать "AI (Лёгкий)" → "AI (easy)"
        val difficultyEasy = stringResource(R.string.ai_difficulty_easy)
        val difficultyHard = stringResource(R.string.ai_difficulty_hard)
        val formattedOpponentNick = opponentNick.replace("Лёгкий", difficultyEasy).replace("Сложный", difficultyHard)

        GameInfo(
            playerNickname = playerNick,
            opponentNickname = formattedOpponentNick,
            isYourTurn = gameState.isMyTurn
        )

        // Индикатор хода (под GameInfo, по центру)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is AIGameViewModel.AIGameUiState.YourTurn -> {
                    Text(
                        text = stringResource(R.string.game_your_turn_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                is AIGameViewModel.AIGameUiState.AITurn -> {
                    // Анимированный индикатор (пульсация)
                    val infiniteTransition = rememberInfiniteTransition(label = "ai_thinking")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(alpha)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.game_ai_turn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {}
            }
        }

        // Поле противника (для атак)
        Text(
            text = stringResource(R.string.game_opponent_board),
            style = MaterialTheme.typography.titleMedium
        )
        GameBoardGrid(
            board = gameState.opponentBoard,
            isInteractive = gameState.isMyTurn && uiState is AIGameViewModel.AIGameUiState.YourTurn,
            onCellClick = onCellClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Моё поле (с кораблями)
        Text(
            text = stringResource(R.string.game_your_board),
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
                text = "Инициализация игры...",
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
