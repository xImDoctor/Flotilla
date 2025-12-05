package com.imdoctor.flotilla.data.remote.websocket

import com.imdoctor.flotilla.data.remote.server.ServerConfig
import com.imdoctor.flotilla.data.remote.websocket.models.WSEvent
import com.imdoctor.flotilla.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "WebSocketManager"

/**
 * Менеджер для управления WebSocket соединениями
 *
 * Управляет двумя типами соединений:
 * - Matchmaking: для поиска игроков
 * - Game: для игрового процесса
 */
class WebSocketManager {
    private var matchmakingClient: WebSocketClient? = null
    private var gameClient: WebSocketClient? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /**
     * Подключиться к matchmaking WebSocket
     *
     * @param token Firebase ID token для аутентификации
     * @param onConnected Callback вызывается после успешного подключения
     * @return Flow с WebSocket событиями
     */
    fun connectToMatchmaking(token: String, onConnected: (() -> Unit)? = null): Flow<WSEvent> {
        Logger.i(TAG, "Connecting to matchmaking")
        _connectionState.value = ConnectionState.Connecting

        val url = ServerConfig.WS_URL + ServerConfig.WebSocket.MATCHMAKING
        matchmakingClient = WebSocketClient(
            url = url,
            token = token
        )

        _connectionState.value = ConnectionState.Connected
        return matchmakingClient!!.connect(onConnected = onConnected)
    }

    /**
     * Подключиться к игровому WebSocket
     *
     * @param gameId ID игры
     * @param token Firebase ID token для аутентификации
     * @param onConnected Callback вызывается после успешного подключения
     * @return Flow с WebSocket событиями
     */
    fun connectToGame(gameId: String, token: String, onConnected: (() -> Unit)? = null): Flow<WSEvent> {
        Logger.i(TAG, "Connecting to game: $gameId")
        _connectionState.value = ConnectionState.Connecting

        val url = ServerConfig.WS_URL + ServerConfig.WebSocket.game(gameId)
        gameClient = WebSocketClient(
            url = url,
            token = token
        )

        _connectionState.value = ConnectionState.Connected
        return gameClient!!.connect(onConnected = onConnected)
    }

    /**
     * Отправить событие в matchmaking
     *
     * @param event WebSocket событие
     */
    fun sendToMatchmaking(event: WSEvent) {
        matchmakingClient?.send(event)
    }

    /**
     * Отправить событие в игру
     *
     * @param event WebSocket событие
     */
    fun sendToGame(event: WSEvent) {
        gameClient?.send(event)
    }

    /**
     * Отключиться от matchmaking
     */
    fun disconnectMatchmaking() {
        Logger.i(TAG, "Disconnecting from matchmaking")
        matchmakingClient?.disconnect()
        matchmakingClient = null
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Отключиться от игры
     */
    fun disconnectGame() {
        Logger.i(TAG, "Disconnecting from game")
        gameClient?.disconnect()
        gameClient = null
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Проверка подключения к matchmaking
     *
     * @return true если подключено к matchmaking
     */
    fun isMatchmakingConnected(): Boolean {
        return matchmakingClient?.isConnected() == true
    }

    /**
     * Проверка подключения к игре
     *
     * @return true если подключено к игре
     */
    fun isGameConnected(): Boolean {
        return gameClient?.isConnected() == true
    }

    /**
     * Состояние подключения
     */
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
