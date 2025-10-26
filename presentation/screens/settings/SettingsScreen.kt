package com.imdoctor.flotilla.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Композ экрана настроект
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Никнейм
            var nickname by remember { mutableStateOf("Player") }
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Divider()

            // Настройки сетки
            Text(
                text = "Настройки сетки",
                style = MaterialTheme.typography.titleMedium
            )

            var showCoordinates by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Показывать координаты")
                Switch(
                    checked = showCoordinates,
                    onCheckedChange = { showCoordinates = it }
                )
            }

            Divider()

            // Звук и анимации
            Text(
                text = "Аудио и эффекты",
                style = MaterialTheme.typography.titleMedium
            )

            var soundEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Звуковые эффекты")
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }

            var animationsEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Анимация")
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = { animationsEnabled = it }
                )
            }

        }
    }
}