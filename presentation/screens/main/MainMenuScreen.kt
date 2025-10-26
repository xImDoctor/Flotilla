package com.imdoctor.flotilla.presentation.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


// Композ для главного меню: заголовок + подзаголовок, текст для кнопок + кнопки
@Composable
fun MainMenuScreen(onNewGame: (String) -> Unit, onFindOpponent: () -> Unit, onStatistics: () -> Unit, onSettings: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Заголовок
        Text(
            text = "FLOTILLA",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Морской бой",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Кнопки меню
        MenuButton(
            text = "Игра против ИИ",
            onClick = { onNewGame("vs_ai") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Найти соперника",
            onClick = onFindOpponent
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Статистика",
            onClick = onStatistics
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}


// Обёртка для быстрого создания кнопок меню
@Composable
private fun MenuButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }

}