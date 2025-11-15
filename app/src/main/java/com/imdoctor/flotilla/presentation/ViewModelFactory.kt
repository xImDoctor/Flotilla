package com.imdoctor.flotilla.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.presentation.screens.settings.SettingsViewModel
import com.imdoctor.flotilla.presentation.screens.stats.StatisticsViewModel
import com.imdoctor.flotilla.presentation.screens.registration.UserRegistrationViewModel

/**
 * Factory для создания ViewModels с зависимостями
 *
 * Позволяет передавать репозитории в конструкторы ViewModels
 */
class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsRepository = AppContainer.settingsRepository
                ) as T
            }

            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(
                    userRepository = AppContainer.userRepository
                ) as T
            }

            modelClass.isAssignableFrom(UserRegistrationViewModel::class.java) -> {
                UserRegistrationViewModel(
                    userRepository = AppContainer.userRepository,
                    authManager = AppContainer.authManager
                ) as T
            }

            modelClass.isAssignableFrom(StartupViewModel::class.java) -> {
                StartupViewModel(
                    userRepository = AppContainer.userRepository
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}