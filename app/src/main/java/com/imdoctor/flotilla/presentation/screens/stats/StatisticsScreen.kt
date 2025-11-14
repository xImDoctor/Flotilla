package com.imdoctor.flotilla.presentation.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.presentation.ViewModelFactory
import kotlin.math.roundToInt

/**
 * Экран статистики с данными из Firebase
 * 
 * Отображает:
 * - Общую статистику (игры, победы, поражения)
 * - Процент побед
 * - Точность стрельбы
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = ViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            
            is StatisticsUiState.Success -> {
                StatisticsContent(
                    profile = state.profile,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is StatisticsUiState.Empty -> {
                EmptyState(modifier = Modifier.padding(padding))
            }
            
            is StatisticsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadStatistics() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

/**
 * Состояние загрузки
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Контент статистики с данными
 */
@Composable
private fun StatisticsContent(
    profile: com.imdoctor.flotilla.data.remote.firebase.models.UserProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Всего игр
        StatCard(
            label = stringResource(R.string.statistics_total_games),
            value = profile.gamesPlayed.toString()
        )
        
        // Побед и Поражений в одном ряду
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.statistics_wins),
                value = profile.wins.toString(),
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                label = stringResource(R.string.statistics_losses),
                value = profile.losses.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Процент побед
        StatCard(
            label = stringResource(R.string.statistics_win_rate),
            value = "${profile.winRate.roundToInt()}%"
        )
        
        // Точность попаданий
        StatCard(
            label = stringResource(R.string.statistics_accuracy),
            value = "${profile.accuracy.roundToInt()}%"
        )
        
        // Дополнительная статистика
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Детальная статистика",
            style = MaterialTheme.typography.titleMedium
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(
                    label = "Всего выстрелов:",
                    value = profile.totalShots.toString()
                )
                DetailRow(
                    label = "Успешных попаданий:",
                    value = profile.successfulShots.toString()
                )
                DetailRow(
                    label = "Никнейм:",
                    value = profile.nickname
                )
            }
        }
    }
}

/**
 * Пустое состояние (нет игр)
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.statistics_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Состояние ошибки
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
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
                text = "Ошибка загрузки статистики",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

/**
 * Карточка статистики
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

/**
 * Строка детальной информации
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
