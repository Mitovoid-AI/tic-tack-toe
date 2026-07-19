package com.tictactoe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tictactoe.TicTacToeApp
import com.tictactoe.data.local.GameResult
import com.tictactoe.ui.screens.GameScreen
import com.tictactoe.ui.screens.HomeScreen
import com.tictactoe.ui.screens.LeaderboardScreen
import com.tictactoe.ui.screens.OnlineGameScreen
import com.tictactoe.ui.screens.OnlineLobbyScreen
import com.tictactoe.ui.screens.ReplayScreen
import com.tictactoe.ui.screens.SettingsScreen
import com.tictactoe.ui.screens.StatsScreen

object Routes {
    const val HOME = "home"
    const val GAME = "game/{mode}"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val REPLAY = "replay/{gameId}"
    const val ONLINE_LOBBY = "online_lobby"
    const val ONLINE_GAME = "online_game/{roomCode}/{isCreator}"
    const val LEADERBOARD = "leaderboard"

    fun game(mode: String) = "game/$mode"
    fun replay(gameId: Long) = "replay/$gameId"
    fun onlineGame(roomCode: String, isCreator: Boolean) = "online_game/$roomCode/$isCreator"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPvPClick = { navController.navigate(Routes.game("pvp")) },
                onAIClick = { navController.navigate(Routes.game("ai")) },
                onOnlineClick = { navController.navigate(Routes.ONLINE_LOBBY) },
                onStatsClick = { navController.navigate(Routes.STATS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onLeaderboardClick = { navController.navigate(Routes.LEADERBOARD) }
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
            StatsScreen(
                onBack = { navController.popBackStack() },
                onReplayClick = { gameId -> navController.navigate(Routes.replay(gameId)) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.REPLAY,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            var gameResult by remember { mutableStateOf<GameResult?>(null) }

            LaunchedEffect(gameId) {
                gameResult = TicTacToeApp.instance.gameRepository.getById(gameId)
            }

            gameResult?.let { result ->
                ReplayScreen(
                    gameResult = result,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ONLINE_LOBBY) {
            OnlineLobbyScreen(
                onBack = { navController.popBackStack() },
                onRoomJoined = { roomCode, isCreator ->
                    navController.navigate(Routes.onlineGame(roomCode, isCreator))
                }
            )
        }

        composable(
            route = Routes.ONLINE_GAME,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("isCreator") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val isCreator = backStackEntry.arguments?.getBoolean("isCreator") ?: false
            OnlineGameScreen(
                roomCode = roomCode,
                isCreator = isCreator,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
