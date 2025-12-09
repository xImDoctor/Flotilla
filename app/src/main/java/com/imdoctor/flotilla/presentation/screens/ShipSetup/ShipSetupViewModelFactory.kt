package com.imdoctor.flotilla.presentation.screens.shipsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imdoctor.flotilla.di.AppContainer

/**
 * Factory для создания ShipSetupViewModel с зависимостями
 */
class ShipSetupViewModelFactory(
    private val gameMode: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShipSetupViewModel::class.java)) {
            return ShipSetupViewModel(
                gameMode = gameMode,
                settingsRepository = AppContainer.settingsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
