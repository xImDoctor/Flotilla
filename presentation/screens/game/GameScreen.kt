package com.imdoctor.flotilla.presentation.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Защита от скриншотов (для пвп режима)
import com.imdoctor.flotilla.utils.security.SecureScreen


// Композ для игровой карты (матча)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(gameId: String, onGameEnd: () -> Unit, onExitGame: () -> Unit) {

    // вызываем лок на скришноты
    SecureScreen()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Происходит бой") },
                actions = {
                    TextButton(onClick = onExitGame) {
                        Text("Выйти")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Игровой экран",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Game ID: $gameId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Заглушка для игрового поля
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.size(300.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Вражеская сетка\n(В будущих обновлениях...)")
                    }
                }

                Card(
                    modifier = Modifier.size(300.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ваша сетка\n(В будущих обновлениях...)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // временная кнопка завершения игры
            Button(
                onClick = onGameEnd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Завершить игру (Debug)")
            }

        }
    }
}
