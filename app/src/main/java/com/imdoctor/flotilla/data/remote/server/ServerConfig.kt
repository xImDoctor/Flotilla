package com.imdoctor.flotilla.data.remote.server

import com.imdoctor.flotilla.BuildConfig

/**
 * Конфигурация сервера Flotilla
 *
 * Конфигурация загружается из local.properties (НЕ коммитится в Git!)
 * Укажите flotilla.server.production.url=http://YOUR_SERVER_IP:8000
 */
object ServerConfig {
    /**
     * Production сервер URL (из local.properties)
     */
    private val SERVER_URL = BuildConfig.SERVER_PRODUCTION_URL

    /**
     * Базовый HTTP URL
     */
    val BASE_URL: String = SERVER_URL

    /**
     * WebSocket URL
     */
    val WS_URL: String = SERVER_URL
        .replace("https://", "wss://")
        .replace("http://", "ws://")

    // API Endpoints
    object Api {
        const val HEALTH = "/health"
        const val VALIDATE_SHIPS = "/api/validate-ships"
    }

    // WebSocket Endpoints
    object WebSocket {
        const val MATCHMAKING = "/ws/matchmaking"
        fun game(gameId: String) = "/ws/game/$gameId"
    }

    /**
     * Таймаут подключения (секунды)
     */
    const val CONNECT_TIMEOUT = 10L

    /**
     * Таймаут чтения (секунды)
     */
    const val READ_TIMEOUT = 30L

    /**
     * Таймаут записи (секунды)
     */
    const val WRITE_TIMEOUT = 30L

    /**
     * WebSocket ping interval (секунды)
     */
    const val PING_INTERVAL = 20L
}
