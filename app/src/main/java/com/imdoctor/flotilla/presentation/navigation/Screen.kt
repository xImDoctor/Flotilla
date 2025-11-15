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

        // Параметр: режим игры (vs_ai, vs_player, local)
        fun createRoute(gameMode: String): String = "$route/$gameMode"
        const val ARG_GAME_MODE = "game_mode"
    }

    // игра ("катка")
    data object Game : Screen("game") {
        fun createRoute(gameId: String): String = "$route/$gameId"
        const val ARG_GAME_ID = "game_id"
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

    private val GAME_MODE_REGEX = Regex("^(vs_ai|vs_player|local)$")
    private val GAME_ID_REGEX = Regex("^[a-zA-Z0-9_-]{8,32}$")

    fun isValidGameMode(mode: String?): Boolean = mode?.matches(GAME_MODE_REGEX) == true

    fun isValidGameId(id: String?): Boolean = id?.matches(GAME_ID_REGEX) == true

    /**
     * Санитизация строки для предотвращения инъекций
     */
    fun sanitizeString(input: String?): String {
        return input?.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
            ?.take(32) // макс. 32 символа
            ?: ""
    }
}