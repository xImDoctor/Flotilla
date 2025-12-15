package com.imdoctor.flotilla.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val musicTrack by viewModel.musicTrack.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val gridAspectRatio by viewModel.gridAspectRatio.collectAsStateWithLifecycle()
    val nicknameUpdateResult by viewModel.nicknameUpdateResult.collectAsStateWithLifecycle()

    // Локальное состояние для диалога Credits
    var showCreditsDialog by remember { mutableStateOf(false) }

    // Локальное состояние для редактирования никнейма
    var nicknameInput by remember { mutableStateOf(nickname) }

    // Обновляем локальный input когда меняется nickname из ViewModel
    LaunchedEffect(nickname) {
        nicknameInput = nickname
    }

    // Snackbar host для показа сообщений
    val snackbarHostState = remember { SnackbarHostState() }

    // Coroutine scope для показа Snackbar
    val coroutineScope = rememberCoroutineScope()

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

            // Координатная сетка (временно отключена)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Показываем подсказку о том, что фича в доработке
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.settings_show_coordinates_wip),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
            ) {
                SettingsRow(
                    label = stringResource(R.string.settings_show_coordinates),
                    checked = showCoordinates,
                    onCheckedChange = { }, // Пустой callback, так как disabled
                    enabled = false
                )
            }
            
            HorizontalDivider()

            // Пропорции игрового поля
            SettingsSliderRow(
                label = stringResource(R.string.settings_grid_aspect_ratio),
                value = gridAspectRatio,
                onValueChange = { viewModel.setGridAspectRatio(it) },
                valueRange = 0.6f..1.2f,
                steps = 11  // 0.6, 0.65, 0.7, ..., 1.15, 1.2 (12 values, 11 steps between)
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

            // Выбор фонового трека (только если музыка включена)
            if (musicEnabled) {
                MusicTrackSelector(
                    selectedTrack = musicTrack,
                    onTrackSelected = { viewModel.setMusicTrack(it) },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

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

            // Кнопка Credits
            OutlinedButton(
                onClick = { showCreditsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_credits_button))
            }

            // Кнопка сброса настроек
            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_set_default))
            }
        }
    }

    // Диалог Credits
    if (showCreditsDialog) {
        CreditsDialog(onDismiss = { showCreditsDialog = false })
    }
}

/**
 * Переиспользуемый компонент строки настройки с Switch
 *
 * @param label Текст настройки
 * @param checked Состояние переключателя
 * @param onCheckedChange Callback изменения состояния
 * @param enabled Включена ли настройка (влияет на цвет текста и Switch)
 */
@Composable
private fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable { onCheckedChange(!checked) }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/**
 * Компонент выбора фонового трека
 *
 * @param selectedTrack ID выбранного трека
 * @param onTrackSelected Callback выбора трека
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MusicTrackSelector(
    selectedTrack: String,
    onTrackSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val trackOptions = mapOf(
        "cats_cradle" to stringResource(R.string.settings_music_track_cats_cradle),
        "hibiscus" to stringResource(R.string.settings_music_track_hibiscus)
    )

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_music_track_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = trackOptions[selectedTrack] ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                trackOptions.forEach { (trackId, trackName) ->
                    DropdownMenuItem(
                        text = { Text(trackName) },
                        onClick = {
                            onTrackSelected(trackId)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

/**
 * Диалог с информацией об игре и credits
 */
@Composable
private fun CreditsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.credits_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Разработчик
                Text(
                    text = stringResource(R.string.credits_developer_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.credits_developer_name),
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // Музыка
                Text(
                    text = stringResource(R.string.credits_music_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.credits_music_purrple_cat),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.credits_music_link),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.credits_music_license),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.credits_music_chosic),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.credits_close))
            }
        }
    )
}

/**
 * Строка настройки со слайдером
 */
@Composable
private fun SettingsSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}
