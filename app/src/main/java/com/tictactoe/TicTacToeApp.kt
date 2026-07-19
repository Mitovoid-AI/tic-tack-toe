package com.tictactoe

import android.app.Application
import com.tictactoe.config.RemoteConfigManager
import com.tictactoe.data.local.AppDatabase
import com.tictactoe.data.repository.GameRepository
import com.tictactoe.ui.theme.ThemeManager
import com.tictactoe.util.HapticManager
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

        remoteConfigManager = RemoteConfigManager(this)
        database = AppDatabase.create(this)
        gameRepository = GameRepository(database.gameDao())
    }

    companion object {
        lateinit var instance: TicTacToeApp
            private set
    }
}
