package com.imdoctor.flotilla.presentation.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.imdoctor.flotilla.R


// Композ для предварительного размещения кораблей (экран расстановки)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipSetupScreen(gameMode: String, onSetupComplete: (String) -> Unit, onBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ship_setup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.ship_setup_heading),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.ship_setup_game_mode, gameMode),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Заглушка для сетки
            Card(
                modifier = Modifier
                    .size(300.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.ship_setup_grid_placeholder))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // кнопка готовности (временно генерирует фейковый game ID, пока нет сервера, чисто для теста)
            Button(
                onClick = {
                    val fakeGameId = "game_${System.currentTimeMillis()}"
                    onSetupComplete(fakeGameId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ship_setup_ready_button))
            }

        }
    }
}
