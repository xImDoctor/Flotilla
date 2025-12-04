package com.imdoctor.flotilla.data.remote.websocket.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Базовая модель WebSocket события
 *
 * Используется для обмена сообщениями между клиентом и сервером
 */
@Serializable
data class WSEvent(
    val type: String,
    val data: JsonElement? = null,
    val error: String? = null,
    val timestamp: String? = null
)

/**
 * Типы WebSocket событий (соответствуют серверу)
 */
object WSEventType {
    // Client -> Server
    const val JOIN_QUEUE = "join_queue"
    const val LEAVE_QUEUE = "leave_queue"
    const val MAKE_MOVE = "make_move"
    const val RECONNECT = "reconnect"
    const val PONG = "pong"

    // Server -> Client
    const val WAITING = "waiting"
    const val MATCH_FOUND = "match_found"
    const val GAME_START = "game_start"
    const val MOVE_RESULT = "move_result"
    const val OPPONENT_MOVE = "opponent_move"
    const val YOUR_TURN = "your_turn"
    const val GAME_OVER = "game_over"

    // System
    const val OPPONENT_DISCONNECTED = "opponent_disconnected"
    const val OPPONENT_RECONNECTED = "opponent_reconnected"
    const val TIMEOUT = "timeout"
    const val ERROR = "error"
    const val PING = "ping"
}
