package com.imdoctor.flotilla.data.remote.websocket.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные для выполнения хода
 */
@Serializable
data class MoveData(
    val x: Int,
    val y: Int
)

/**
 * Результат выполненного хода
 */
@Serializable
data class MoveResultData(
    val x: Int,
    val y: Int,
    val result: String,  // "miss", "hit", "sunk", "win"
    @SerialName("ship_id")
    val shipId: String? = null,
    @SerialName("ship_length")
    val shipLength: Int? = null,
    @SerialName("game_over")
    val gameOver: Boolean = false,
    @SerialName("winner_id")
    val winnerId: String? = null
)

/**
 * Данные о ходе противника
 */
@Serializable
data class OpponentMoveData(
    val x: Int,
    val y: Int,
    val result: String  // "miss" or "hit"
)

/**
 * Данные о завершении игры
 */
@Serializable
data class GameOverData(
    @SerialName("winner_id")
    val winnerId: String,
    @SerialName("total_moves")
    val totalMoves: Int,
    @SerialName("duration_seconds")
    val durationSeconds: Double
)

/**
 * Результаты ходов (константы)
 */
object MoveResult {
    const val MISS = "miss"
    const val HIT = "hit"
    const val SUNK = "sunk"
    const val WIN = "win"
}
