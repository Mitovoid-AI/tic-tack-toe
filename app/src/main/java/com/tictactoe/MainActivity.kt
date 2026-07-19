package com.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.tictactoe.config.AppConfig
import com.tictactoe.ui.navigation.NavGraph
import com.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TicTacToeApp

        setContent {
            LaunchedEffect(Unit) {
                val config = app.remoteConfigManager.fetchConfig()
                AppConfig.update(config)
            }

            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppConfig.backgroundColor()
                ) {
                    NavGraph()
                }
            }
        }
    }
}
