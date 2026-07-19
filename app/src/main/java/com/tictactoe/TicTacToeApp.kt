package com.tictactoe

import android.app.Application
import com.tictactoe.config.RemoteConfigManager
import com.tictactoe.data.local.AppDatabase
import com.tictactoe.data.repository.GameRepository
import com.tictactoe.ui.theme.ThemeManager
import com.tictactoe.util.HapticManager
import com.tictactoe.util.MarkerManager
import com.tictactoe.util.PrefsManager
import com.tictactoe.util.SoundManager

class TicTacToeApp : Application() {

    lateinit var remoteConfigManager: RemoteConfigManager
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var gameRepository: GameRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        try { ThemeManager.init(this) } catch (_: Exception) {}
        try { PrefsManager.init(this) } catch (_: Exception) {}
        try { HapticManager.init(this) } catch (_: Exception) {}
        try { SoundManager.init(this) } catch (_: Exception) {}
        try { MarkerManager.init(this) } catch (_: Exception) {}

        try {
            remoteConfigManager = RemoteConfigManager(this)
            database = AppDatabase.create(this)
            gameRepository = GameRepository(database.gameDao())
        } catch (e: Exception) {
            // If DB fails, try with fresh database
            try {
                deleteDatabase("tictactoe.db")
                database = AppDatabase.create(this)
                gameRepository = GameRepository(database.gameDao())
                remoteConfigManager = RemoteConfigManager(this)
            } catch (_: Exception) {}
        }
    }

    companion object {
        lateinit var instance: TicTacToeApp
            private set
    }
}
