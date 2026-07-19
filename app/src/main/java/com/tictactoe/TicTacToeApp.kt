package com.tictactoe

import android.app.Application
import com.tictactoe.config.RemoteConfigManager
import com.tictactoe.data.local.AppDatabase
import com.tictactoe.data.repository.GameRepository

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
        remoteConfigManager = RemoteConfigManager(this)
        database = AppDatabase.create(this)
        gameRepository = GameRepository(database.gameDao())
    }

    companion object {
        lateinit var instance: TicTacToeApp
            private set
    }
}
