package com.tictactoe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tictactoe.ui.screens.GameScreen
import com.tictactoe.ui.screens.HomeScreen
import com.tictactoe.ui.screens.SettingsScreen
import com.tictactoe.ui.screens.StatsScreen

object Routes {
    const val HOME = "home"
    const val GAME = "game/{mode}"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun game(mode: String) = "game/$mode"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPvPClick = { navController.navigate(Routes.game("pvp")) },
                onAIClick = { navController.navigate(Routes.game("ai")) },
                onStatsClick = { navController.navigate(Routes.STATS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "pvp"
            GameScreen(
                mode = mode,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
