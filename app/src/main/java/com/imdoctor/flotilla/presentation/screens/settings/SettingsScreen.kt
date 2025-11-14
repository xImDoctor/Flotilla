package com.imdoctor.flotilla.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.presentation.ViewModelFactory

/**
 * Экран настроек с интеграцией Firebase и DataStore
 * 
 * Настройки автоматически сохраняются локально и синхронизируются с Firebase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory())
) {
    // Получаем значения из ViewModel через collectAsStateWithLifecycle
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val showCoordinates by viewModel.showCoordinates.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.common_back))
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
            OutlinedTextField(
                value = nickname,
                onValueChange = { viewModel.updateNickname(it) },
                label = { Text(stringResource(R.string.settings_nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Настройки сетки
            Text(
                text = stringResource(R.string.settings_grid_title),
                style = MaterialTheme.typography.titleMedium
            )
            
            SettingsRow(
                label = stringResource(R.string.settings_show_coordinates),
                checked = showCoordinates,
                onCheckedChange = { viewModel.toggleShowCoordinates(it) }
            )
            
            HorizontalDivider()
            
            // Аудио и эффекты
            Text(
                text = stringResource(R.string.settings_audio_effects_title),
                style = MaterialTheme.typography.titleMedium
            )
            
            SettingsRow(
                label = stringResource(R.string.settings_sound_effects),
                checked = soundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )
            
            SettingsRow(
                label = stringResource(R.string.settings_animations),
                checked = animationsEnabled,
                onCheckedChange = { viewModel.toggleAnimations(it) }
            )
            
            SettingsRow(
                label = "Вибрация",
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration(it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сброса настроек
            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить к умолчаниям")
            }
        }
    }
}

/**
 * Переиспользуемый компонент строки настройки с Switch
 * 
 * @param label Текст настройки
 * @param checked Состояние переключателя
 * @param onCheckedChange Callback изменения состояния
 */
@Composable
private fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
