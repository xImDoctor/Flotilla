package com.imdoctor.flotilla.data.remote.server

import com.imdoctor.flotilla.BuildConfig

/**
 * Конфигурация сервера Flotilla
 *
 * Конфигурация загружается из local.properties (НЕ коммитится в Git!)
 * См. local.properties.example для инструкций
 *
 * !!!!!! Устройство и ПК должны быть в одной WiFi сети!!!!!!!!
 */
object ServerConfig {
    /**
     * Локальный IP адрес вашего ПК (из local.properties)
     *
     * !!!!!!!!! в local.properties:
     * flotilla.server.local.ip=IP
     */
    private val LOCAL_IP = BuildConfig.SERVER_LOCAL_IP

    /**
     * Порт сервера (из local.properties)
     */
    private val LOCAL_PORT = BuildConfig.SERVER_LOCAL_PORT

    /**
     * Production URL (из local.properties, для будущего)
     */
    private val PRODUCTION_URL = BuildConfig.SERVER_PRODUCTION_URL

    /**
     * Переключатель между локальной разработкой и production
     *
     * В debug build всегда используется локальный сервер
     * В release build используется production
     */
    val USE_LOCAL = BuildConfig.DEBUG

    /**
     * Базовый HTTP URL
     */
    val BASE_URL: String
        get() = if (USE_LOCAL) {
            "http://$LOCAL_IP:$LOCAL_PORT"
        } else {
            PRODUCTION_URL
        }

    /**
     * WebSocket URL
     */
    val WS_URL: String
        get() = if (USE_LOCAL) {
            "ws://$LOCAL_IP:$LOCAL_PORT"
        } else {
            PRODUCTION_URL.replace("https://", "wss://")
        }

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
