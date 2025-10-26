package com.imdoctor.flotilla.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument


import com.imdoctor.flotilla.presentation.screens.main.MainMenuScreen
import com.imdoctor.flotilla.presentation.screens.settings.SettingsScreen
import com.imdoctor.flotilla.presentation.screens.stats.StatisticsScreen
import com.imdoctor.flotilla.presentation.screens.setup.ShipSetupScreen
import com.imdoctor.flotilla.presentation.screens.game.GameScreen
import com.imdoctor.flotilla.presentation.screens.matchmaking.FindOpponentScreen


@Composable
fun FlotillaNavGraph(navController: NavHostController, startDestination: String = Screen.MainMenu.route) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // главное меню
        composable(Screen.MainMenu.route) {
            MainMenuScreen(

                onNewGame = { gameMode ->
                    // валидация режима игры
                    if (NavigationValidator.isValidGameMode(gameMode)) {
                        navController.navigate(Screen.ShipSetup.createRoute(gameMode))
                    }
                },

                onFindOpponent = {
                    navController.navigate(Screen.FindOpponent.route)
                },

                onStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },

                onSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // настройки
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // постановка короблей
        composable(
            route = "${Screen.ShipSetup.route}/{${Screen.ShipSetup.ARG_GAME_MODE}}",
            arguments = listOf(
                navArgument(Screen.ShipSetup.ARG_GAME_MODE) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val gameMode = backStackEntry.arguments?.getString(Screen.ShipSetup.ARG_GAME_MODE)

            // валидация аргументов
            if (NavigationValidator.isValidGameMode(gameMode)) {
                ShipSetupScreen(
                    gameMode = gameMode!!,
                    onSetupComplete = { gameId ->

                        // Переход к игре с валидированным ID
                        if (NavigationValidator.isValidGameId(gameId)) {
                            navController.navigate(Screen.Game.createRoute(gameId)) {

                                // Очистка back stack для предотвращения возврата к setup
                                popUpTo(Screen.MainMenu.route)
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                // В случае невалидного аргумента - возврат в меню
                navController.popBackStack()
            }
        }

        // игра (матч)
        composable(
            route = "${Screen.Game.route}/{${Screen.Game.ARG_GAME_ID}}",
            arguments = listOf(
                navArgument(Screen.Game.ARG_GAME_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(Screen.Game.ARG_GAME_ID)

            if (NavigationValidator.isValidGameId(gameId)) {
                GameScreen(
                    gameId = gameId!!,
                    onGameEnd = {
                        // После окончания игры - в статистику
                        navController.navigate(Screen.Statistics.route) {
                            popUpTo(Screen.MainMenu.route)
                        }
                    },
                    onExitGame = {
                        navController.popBackStack(Screen.MainMenu.route, inclusive = false)
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        // статистика
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // матчмэйкинг
        composable(Screen.FindOpponent.route) {
            FindOpponentScreen(
                onOpponentFound = { gameId ->
                    if (NavigationValidator.isValidGameId(gameId)) {
                        navController.navigate(Screen.ShipSetup.createRoute("vs_player"))
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}