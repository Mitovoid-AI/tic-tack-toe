package com.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tictactoe.config.AppConfig
import com.tictactoe.ui.components.UpdateDialog
import com.tictactoe.ui.navigation.NavGraph
import com.tictactoe.ui.theme.TicTacToeTheme
import com.tictactoe.util.UpdateInfo
import com.tictactoe.util.UpdateManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TicTacToeApp

        setContent {
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

            LaunchedEffect(Unit) {
                try {
                    val config = app.remoteConfigManager.fetchConfig()
                    AppConfig.update(config)
                } catch (_: Exception) {}

                try {
                    val update = UpdateManager.checkForUpdate(this@MainActivity)
                    updateInfo = update
                } catch (_: Exception) {}
            }

            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppConfig.backgroundColor()
                ) {
                    NavGraph()
                }

                updateInfo?.let { info ->
                    UpdateDialog(
                        updateInfo = info,
                        onDismiss = { updateInfo = null }
                    )
                }
            }
        }
    }
}
