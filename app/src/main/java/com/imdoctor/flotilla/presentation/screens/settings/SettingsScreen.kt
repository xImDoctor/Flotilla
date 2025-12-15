package com.imdoctor.flotilla.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val musicEnabled by viewModel.musicEnabled.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val nicknameUpdateResult by viewModel.nicknameUpdateResult.collectAsStateWithLifecycle()

    // Локальное состояние для редактирования никнейма
    var nicknameInput by remember { mutableStateOf(nickname) }

    // Обновляем локальный input когда меняется nickname из ViewModel
    LaunchedEffect(nickname) {
        nicknameInput = nickname
    }

    // Snackbar host для показа сообщений
    val snackbarHostState = remember { SnackbarHostState() }

    // Context для доступа к ресурсам
    val context = LocalContext.current

    // Показываем Snackbar при изменении результата
    LaunchedEffect(nicknameUpdateResult) {
        when (val result = nicknameUpdateResult) {
            is NicknameUpdateResult.Success -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.settings_nickname_success),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearNicknameUpdateResult()
            }
            is NicknameUpdateResult.Error -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(result.messageResId),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearNicknameUpdateResult()
            }
            else -> { /* Idle или Loading */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Никнейм с кнопкой сохранения
            Text(
                text = stringResource(R.string.settings_nickname_title),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text(stringResource(R.string.settings_nickname_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = nicknameUpdateResult !is NicknameUpdateResult.Loading,
                    supportingText = {
                        Text(stringResource(R.string.settings_nickname_hint))
                    }
                )

                // Кнопка сохранения
                FilledTonalButton(
                    onClick = { viewModel.updateNickname(nicknameInput) },
                    enabled = nicknameUpdateResult !is NicknameUpdateResult.Loading &&
                            nicknameInput.trim() != nickname,
                    modifier = Modifier.height(56.dp)
                ) {
                    if (nicknameUpdateResult is NicknameUpdateResult.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.settings_nickname_save)
                        )
                    }
                }
            }

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
                label = stringResource(R.string.settings_background_music),
                checked = musicEnabled,
                onCheckedChange = { viewModel.toggleMusic(it) }
            )

            SettingsRow(
                label = stringResource(R.string.settings_sound_effects),
                checked = soundEffectsEnabled,
                onCheckedChange = { viewModel.toggleSoundEffects(it) }
            )
            
            SettingsRow(
                label = stringResource(R.string.settings_animations),
                checked = animationsEnabled,
                onCheckedChange = { viewModel.toggleAnimations(it) }
            )
            
            SettingsRow(
                label = stringResource(R.string.settings_vibration),
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration(it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сброса настроек
            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_set_default))
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
