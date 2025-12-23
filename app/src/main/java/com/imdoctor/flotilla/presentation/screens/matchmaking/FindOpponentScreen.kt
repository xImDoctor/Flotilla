package com.imdoctor.flotilla.presentation.screens.matchmaking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.presentation.ViewModelFactory

/**
 * Экран поиска соперника
 *
 * Отображает различные состояния matchmaking:
 * - Idle: готов начать поиск
 * - Connecting: подключение к серверу
 * - Waiting: ожидание в очереди
 * - MatchFound: матч найден, переход к игре
 * - Error: ошибка подключения или поиска
 *
 * @param onOpponentFound Callback при найденном матче (gameId)
 * @param onCancel Callback при отмене поиска
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindOpponentScreen(
    onOpponentFound: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: MatchmakingViewModel = viewModel(factory = ViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    // Обрабатываем найденный матч
    LaunchedEffect(uiState) {
        android.util.Log.d("FindOpponentScreen", "LaunchedEffect triggered, state: ${uiState::class.simpleName}")
        if (uiState is MatchmakingViewModel.MatchmakingUiState.MatchFound) {
            val matchFound = uiState as MatchmakingViewModel.MatchmakingUiState.MatchFound
            android.util.Log.i("FindOpponentScreen", "Match found! Calling onOpponentFound with gameId: ${matchFound.gameId}")
            onOpponentFound(matchFound.gameId)
        }
    }

    // Гарантированная очистка при выходе из экрана
    DisposableEffect(Unit) {
        onDispose {
            // Отменить поиск только если матч НЕ найден
            // Если матч найден, WebSocket нужен для перехода к игре
            if (uiState !is MatchmakingViewModel.MatchmakingUiState.MatchFound) {
                viewModel.cancelMatchmaking()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.find_opponent_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelMatchmaking()
                        onCancel()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is MatchmakingViewModel.MatchmakingUiState.Idle -> {
                    IdleContent(
                        onStartSearch = {
                            viewModel.startMatchmaking()
                        },
                        onCancel = onCancel
                    )
                }

                is MatchmakingViewModel.MatchmakingUiState.Connecting -> {
                    ConnectingContent()
                }

                is MatchmakingViewModel.MatchmakingUiState.Waiting -> {
                    WaitingContent(
                        position = state.position,
                        queueSize = state.queueSize,
                        onCancel = {
                            viewModel.cancelMatchmaking()
                        },
                        onJoinQueue = {
                            viewModel.joinQueue()
                        }
                    )
                }

                is MatchmakingViewModel.MatchmakingUiState.MatchFound -> {
                    MatchFoundContent(
                        opponentNickname = state.opponentNickname
                    )
                }

                is MatchmakingViewModel.MatchmakingUiState.Error -> {
                    ErrorContent(
                        errorMessage = state.message,
                        onRetry = {
                            viewModel.startMatchmaking()
                        },
                        onCancel = onCancel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Информация о требовании интернета
            Text(
                text = stringResource(R.string.find_opponent_internet_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IdleContent(
    onStartSearch: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = stringResource(R.string.find_opponent_ready_prompt),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onStartSearch,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.find_opponent_start_search))
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.find_opponent_back_to_menu))
    }
}

@Composable
private fun ConnectingContent() {
    CircularProgressIndicator(
        modifier = Modifier.size(64.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.find_opponent_connecting),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WaitingContent(
    position: Int,
    queueSize: Int?,
    onCancel: () -> Unit,
    onJoinQueue: () -> Unit
) {
    CircularProgressIndicator(
        modifier = Modifier.size(64.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.find_opponent_searching),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Показываем позицию в очереди
    if (queueSize != null) {
        Text(
            text = "Позиция в очереди: $position из $queueSize",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    } else {
        Text(
            text = "Позиция в очереди: $position",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(48.dp))

    Button(
        onClick = onCancel
    ) {
        Text(stringResource(R.string.find_opponent_cancel_search))
    }
}

@Composable
private fun MatchFoundContent(
    opponentNickname: String
) {
    Icon(
        imageVector = Icons.Default.ArrowBack, // TODO: Заменить на иконку галочки/успеха
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Соперник найден!",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Противник: $opponentNickname",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    CircularProgressIndicator(
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Подготовка игры...",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = "Ошибка подключения",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Повторить попытку")
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.find_opponent_back_to_menu))
    }
}
