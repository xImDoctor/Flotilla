package com.imdoctor.flotilla.presentation.screens.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.imdoctor.flotilla.presentation.theme.FlotillaColors

/**
 * Экран регистрации пользователя при первом запуске
 *
 * Позволяет выбрать никнейм и создать профиль игрока
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(
    onRegistrationComplete: () -> Unit,
    viewModel: UserRegistrationViewModel = viewModel(factory = ViewModelFactory())
) {
    // Собираем состояние из ViewModel
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val registrationComplete by viewModel.registrationComplete.collectAsStateWithLifecycle()

    // Обработка успешной регистрации
    LaunchedEffect(registrationComplete) {
        if (registrationComplete) {
            onRegistrationComplete()
        }
    }

    Scaffold(
        containerColor = FlotillaColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Заголовок с темой приложения
                Text(
                    text = stringResource(R.string.registration_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = FlotillaColors.Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.registration_subtitle),
                    style = MaterialTheme.typography.headlineMedium,
                    color = FlotillaColors.OnBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Приветственное сообщение
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = FlotillaColors.Surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.registration_welcome_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = FlotillaColors.OnSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Поле ввода никнейма
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { viewModel.updateNickname(it) },
                    label = { Text(stringResource(R.string.registration_nickname_label)) },
                    placeholder = { Text(stringResource(R.string.registration_nickname_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = FlotillaColors.Error
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.registration_nickname_hint),
                                color = FlotillaColors.OnSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlotillaColors.Primary,
                        unfocusedBorderColor = FlotillaColors.OnSurface.copy(alpha = 0.3f),
                        cursorColor = FlotillaColors.Primary,
                        focusedLabelColor = FlotillaColors.Primary,
                        unfocusedLabelColor = FlotillaColors.OnSurface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка начала игры
                Button(
                    onClick = { viewModel.completeRegistration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && nickname.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlotillaColors.Primary,
                        contentColor = FlotillaColors.OnPrimary,
                        disabledContainerColor = FlotillaColors.Surface,
                        disabledContentColor = FlotillaColors.OnSurface.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = FlotillaColors.OnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.registration_start_button),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Информация о правилах никнейма
                AnimatedVisibility(
                    visible = nickname.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = FlotillaColors.Primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.registration_rules_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = FlotillaColors.Primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.registration_rules_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = FlotillaColors.OnSurface
                            )
                        }
                    }
                }
            }

            // Индикатор загрузки на весь экран при регистрации
            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FlotillaColors.Background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = FlotillaColors.Surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = FlotillaColors.Primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.registration_creating_profile),
                                style = MaterialTheme.typography.bodyLarge,
                                color = FlotillaColors.OnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
