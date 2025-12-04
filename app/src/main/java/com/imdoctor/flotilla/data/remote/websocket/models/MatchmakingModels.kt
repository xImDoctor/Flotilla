package com.imdoctor.flotilla.data.remote.websocket.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Данные для присоединения к очереди matchmaking
 */
@Serializable
data class JoinQueueData(
    val nickname: String,
    val ships: List<ShipPlacement>
)

/**
 * Расстановка корабля на поле
 *
 * @property x Координата X начальной клетки (0-9)
 * @property y Координата Y начальной клетки (0-9)
 * @property length Длина корабля (1-4)
 * @property orientation Ориентация ("horizontal" или "vertical")
 */
@Serializable
data class ShipPlacement(
    val x: Int,
    val y: Int,
    val length: Int,
    val orientation: String  // "horizontal" или "vertical"
)

/**
 * Данные о позиции в очереди
 */
@Serializable
data class WaitingData(
    val position: Int,
    @SerialName("queue_size")
    val queueSize: Int? = null
)

/**
 * Данные о найденном матче
 */
@Serializable
data class MatchFoundData(
    @SerialName("game_id")
    val gameId: String,
    @SerialName("opponent_id")
    val opponentId: String,
    @SerialName("opponent_nickname")
    val opponentNickname: String,
    @SerialName("your_turn")
    val yourTurn: Boolean
)
