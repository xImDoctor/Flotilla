package com.imdoctor.flotilla.data.repository

import com.imdoctor.flotilla.data.remote.websocket.models.ShipPlacement

/**
 * Временное хранилище данных matchmaking
 *
 * Используется для передачи данных между FindOpponentScreen и GameScreen
 * TODO: В будущем заменить на более надёжное решение (SavedStateHandle, Navigation args, или Repository)
 */
object MatchmakingDataHolder {

    private var cachedData: MatchmakingData? = null
    private var preparedShips: List<ShipPlacement>? = null

    /**
     * Сохранить подготовленные корабли перед матчмейкингом
     */
    fun savePreparedShips(ships: List<ShipPlacement>) {
        preparedShips = ships
    }

    /**
     * Получить подготовленные корабли
     */
    fun getPreparedShips(): List<ShipPlacement>? {
        return preparedShips
    }

    /**
     * Сохранить данные matchmaking
     */
    fun saveMatchData(
        gameId: String,
        opponentNickname: String,
        yourTurn: Boolean,
        myShips: List<ShipPlacement>
    ) {
        cachedData = MatchmakingData(
            gameId = gameId,
            opponentNickname = opponentNickname,
            yourTurn = yourTurn,
            myShips = myShips
        )
    }

    /**
     * Получить данные matchmaking и очистить кэш
     */
    fun getMatchData(gameId: String): MatchmakingData? {
        return if (cachedData?.gameId == gameId) {
            cachedData
        } else {
            null
        }
    }

    /**
     * Очистить кэшированные данные
     */
    fun clear() {
        cachedData = null
    }

    /**
     * Данные найденного матча
     */
    data class MatchmakingData(
        val gameId: String,
        val opponentNickname: String,
        val yourTurn: Boolean,
        val myShips: List<ShipPlacement>
    )
}
