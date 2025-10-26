package com.imdoctor.flotilla.presentation.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Общая статистика
            StatCard(
                title = "Всего игр",
                value = "0"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Побед",
                    value = "0",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )

                StatCard(
                    title = "Поражений",
                    value = "0",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error
                )
            }

            StatCard(
                title = "Процент побед",
                value = "0%"
            )


            StatCard(
                title = "Точность попаданий",
                value = "0%"
            )


            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Сыграйте матч, чтобы обновить статистику",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

    }
}

// Общая обёртка для "полей" статистики
@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier,
                     color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }


}