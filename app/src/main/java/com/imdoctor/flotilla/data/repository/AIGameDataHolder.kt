package com.imdoctor.flotilla.data.repository

import com.imdoctor.flotilla.data.remote.websocket.models.ShipPlacement

/**
 * Временное хранилище данных AI игры для передачи между экранами
 *
 * Аналогично MatchmakingDataHolder, но для AI режима.
 * Хранит корабли игрока после расстановки для передачи в GameScreen.
 */
object AIGameDataHolder {

    /**
     * Данные AI игры
     */
    data class AIGameData(
        val gameId: String,
        val playerShips: List<ShipPlacement>,
        val difficulty: String  // "ai_easy" или "ai_hard"
    )

    private val gameDataMap = mutableMapOf<String, AIGameData>()

    /**
     * Сохранить данные AI игры
     *
     * @param gameId ID игры
     * @param data Данные игры
     */
    fun saveGameData(gameId: String, data: AIGameData) {
        gameDataMap[gameId] = data
    }

    /**
     * Получить данные AI игры
     *
     * @param gameId ID игры
     * @return Данные игры или null если не найдены
     */
    fun getGameData(gameId: String): AIGameData? {
        return gameDataMap[gameId]
    }

    /**
     * Удалить данные игры (после завершения)
     *
     * @param gameId ID игры
     */
    fun clearGameData(gameId: String) {
        gameDataMap.remove(gameId)
    }

    /**
     * Очистить все данные
     */
    fun clearAll() {
        gameDataMap.clear()
    }
}
