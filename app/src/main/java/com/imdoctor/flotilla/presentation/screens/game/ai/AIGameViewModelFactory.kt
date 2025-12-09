package com.imdoctor.flotilla.presentation.screens.game.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imdoctor.flotilla.data.remote.websocket.models.ShipPlacement

/**
 * Factory для создания AIGameViewModel с зависимостями
 */
class AIGameViewModelFactory(
    private val difficulty: AIDifficulty,
    private val playerShips: List<ShipPlacement>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIGameViewModel::class.java)) {
            return AIGameViewModel(
                difficulty = difficulty,
                playerShips = playerShips
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
