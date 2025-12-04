package com.imdoctor.flotilla.data.remote.websocket

import com.imdoctor.flotilla.data.remote.websocket.models.WSEvent
import com.imdoctor.flotilla.utils.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private const val TAG = "WebSocketClient"

/**
 * WebSocket клиент для общения с сервером Flotilla
 *
 * Использует OkHttp для управления WebSocket соединением
 *
 * @property url WebSocket URL (без параметров)
 * @property token Firebase ID token для аутентификации
 */
class WebSocketClient(
    private val url: String,
    private val token: String
) {
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)  // Keep-alive
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Подключиться к WebSocket и получать события
     *
     * @return Flow с WebSocket событиями
     */
    fun connect(): Flow<WSEvent> = callbackFlow {
        val fullUrl = "$url?token=$token"
        Logger.d(TAG, "Connecting to: $url")

        val request = Request.Builder()
            .url(fullUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Logger.i(TAG, "WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Logger.d(TAG, "Received: $text")
                try {
                    val event = json.decodeFromString<WSEvent>(text)
                    trySend(event).isSuccess
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to parse message: $text", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Logger.e(TAG, "WebSocket error: ${response?.code}", t)
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Logger.i(TAG, "WebSocket closed: $code - $reason")
                channel.close()
            }
        })

        awaitClose {
            Logger.d(TAG, "Closing WebSocket")
            webSocket?.close(1000, "Client closed")
        }
    }

    /**
     * Отправить событие на сервер
     *
     * @param event WebSocket событие
     */
    fun send(event: WSEvent) {
        try {
            val jsonString = json.encodeToString(event)
            Logger.d(TAG, "Sending: $jsonString")
            webSocket?.send(jsonString)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to send event", e)
        }
    }

    /**
     * Отключиться от WebSocket
     */
    fun disconnect() {
        Logger.d(TAG, "Disconnecting")
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    /**
     * Проверка подключения
     *
     * @return true если WebSocket подключен
     */
    fun isConnected(): Boolean {
        return webSocket != null
    }
}
