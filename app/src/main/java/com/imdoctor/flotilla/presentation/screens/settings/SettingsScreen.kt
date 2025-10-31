package com.imdoctor.flotilla.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.imdoctor.flotilla.R

// Композ экрана настроек
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Никнейм
            var nickname by remember { mutableStateOf(stringResource(R.string.settings_nickname_default)) }
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(R.string.settings_nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // Настройки сетки
            Text(
                text = stringResource(R.string.settings_grid_title),
                style = MaterialTheme.typography.titleMedium
            )

            var showCoordinates by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCoordinates = !showCoordinates },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_show_coordinates))
                Switch(
                    checked = showCoordinates,
                    onCheckedChange = { showCoordinates = it }
                )
            }

            HorizontalDivider()

            // Звук и анимации
            Text(
                text = stringResource(R.string.settings_audio_effects_title),
                style = MaterialTheme.typography.titleMedium
            )

            var soundEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { soundEnabled = !soundEnabled },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_sound_effects))
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }

            var animationsEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { animationsEnabled = !animationsEnabled },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_animations))
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = { animationsEnabled = it }
                )
            }

        }
    }
}