package com.tictactoe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.TicTacToeApp
import com.tictactoe.config.AppConfig
import com.tictactoe.ui.components.GlassCard
import com.tictactoe.ui.components.NeonButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = TicTacToeApp.instance.gameRepository
    val configManager = TicTacToeApp.instance.remoteConfigManager

    var refreshStatus by remember { mutableStateOf("") }

    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val accent = AppConfig.accentColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textSecondary)
            }
            Text(
                text = "Settings",
                color = primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Theme preview
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Current Theme", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Colors", color = textSecondary, fontSize = 13.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    ColorDot(AppConfig.backgroundColor(), "BG")
                    ColorDot(AppConfig.surfaceColor(), "Surface")
                    ColorDot(primary, "Primary")
                    ColorDot(secondary, "Secondary")
                    ColorDot(accent, "Accent")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Board: ${AppConfig.board.size}x${AppConfig.board.size}", color = textSecondary, fontSize = 13.sp)
                Text("AI Difficulty: ${AppConfig.features.ai_difficulty}", color = textSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Actions", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                NeonButton(
                    text = "REFRESH CONFIG",
                    onClick = {
                        scope.launch {
                            refreshStatus = "Refreshing..."
                            configManager.clearCache()
                            val config = configManager.fetchConfig()
                            AppConfig.update(config)
                            refreshStatus = "Config updated!"
                        }
                    },
                    color = primary
                )

                if (refreshStatus.isNotEmpty()) {
                    Text(refreshStatus, color = accent, fontSize = 13.sp)
                }

                NeonButton(
                    text = "CLEAR STATS",
                    onClick = {
                        scope.launch {
                            repo.clearAll()
                        }
                    },
                    color = secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App info
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("About", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tic Tac Toe — Neon Edition", color = textSecondary, fontSize = 13.sp)
                Text("Version 1.0.0", color = textSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Text(label, color = AppConfig.textSecondaryColor(), fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
