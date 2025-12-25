package com.imdoctor.flotilla.presentation.navigation

/**
 * Sealed class для безопасной типизированной навигации
 * Для предотвращения ошибки с неправильными маршрутами
 */
sealed class Screen(val route: String) {

    // регистрация пользователя (первый запуск)
    data object UserRegistration : Screen("user_registration")

    // мэйн меню
    data object MainMenu : Screen("main_menu")

    // параметры игрока
    data object Settings : Screen("settings")


    // постановка кораблей
    data object ShipSetup : Screen("ship_setup") {

        // Параметр: режим игры (online, ai_easy, ai_hard)
        fun createRoute(gameMode: String): String = "$route/$gameMode"
        const val ARG_GAME_MODE = "game_mode"
    }

    // игра ("катка")
    data object Game : Screen("game") {
        // gameMode добавлен для различения online и AI режимов
        fun createRoute(gameId: String, gameMode: String = "online"): String = "$route/$gameId/$gameMode"
        const val ARG_GAME_ID = "game_id"
        const val ARG_GAME_MODE = "game_mode"
    }

    // статистика
    data object Statistics : Screen("statistics")

    // матчмэйкинг
    data object FindOpponent : Screen("matchmaking")
}

/**
 * Валидация навигационных аргументов для безопасности
 */
object NavigationValidator {

    private val GAME_MODE_REGEX = Regex("^(online|ai_easy|ai_hard)$")
    // Поддержка UUID формата (36 символов) и других game_id
    private val GAME_ID_REGEX = Regex("^[a-zA-Z0-9_-]{3,64}$")

    fun isValidGameMode(mode: String?): Boolean = mode?.matches(GAME_MODE_REGEX) == true

    fun isValidGameId(id: String?): Boolean = id?.matches(GAME_ID_REGEX) == true

    /**
     * Санитизация строки для предотвращения инъекций
     */
    fun sanitizeString(input: String?): String {
        return input?.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
            ?.take(64) // макс. 64 символа (поддержка UUID)
            ?: ""
    }
}