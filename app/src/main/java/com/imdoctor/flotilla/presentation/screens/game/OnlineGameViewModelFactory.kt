package com.imdoctor.flotilla.presentation.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imdoctor.flotilla.data.remote.websocket.WebSocketManager
import com.imdoctor.flotilla.data.repository.MatchmakingDataHolder
import com.imdoctor.flotilla.data.repository.UserRepository

/**
 * Custom Factory для создания OnlineGameViewModel с параметрами
 *
 * @param gameId Идентификатор игры
 * @param webSocketManager WebSocket менеджер
 * @param userRepository Репозиторий пользователей
 */
class OnlineGameViewModelFactory(
    private val gameId: String,
    private val webSocketManager: WebSocketManager,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineGameViewModel::class.java)) {
            // Получить данные matchmaking
            val matchData = MatchmakingDataHolder.getMatchData(gameId)
                ?: throw IllegalStateException("Match data not found for game $gameId")

            return OnlineGameViewModel(
                gameId = matchData.gameId,
                opponentNickname = matchData.opponentNickname,
                yourTurn = matchData.yourTurn,
                myShips = matchData.myShips,
                webSocketManager = webSocketManager,
                userRepository = userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
