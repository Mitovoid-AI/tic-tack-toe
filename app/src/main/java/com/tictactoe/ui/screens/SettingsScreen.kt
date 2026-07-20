package com.tictactoe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.tictactoe.ui.theme.AppTheme
import com.tictactoe.ui.theme.ThemeManager
import com.tictactoe.util.MarkerManager
import com.tictactoe.util.PrefsManager
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
            .verticalScroll(rememberScrollState())
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

        // Theme Picker
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Theme", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = ThemeManager.currentTheme == theme
                        val themeColors = when (theme) {
                            AppTheme.NEON -> Triple(Color(0xFF0A0A1A), Color(0xFF00D4FF), Color(0xFFFF006E))
                            AppTheme.LIGHT -> Triple(Color(0xFFF5F5F5), Color(0xFF1A73E8), Color(0xFFE91E63))
                            AppTheme.DARK -> Triple(Color(0xFF121212), Color(0xFFBB86FC), Color(0xFF03DAC6))
                            AppTheme.SYSTEM -> Triple(Color(0xFF0A0A1A), Color(0xFF00D4FF), Color(0xFFFF006E))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) primary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) primary else textSecondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { ThemeManager.setTheme(theme) }
                                .padding(12.dp)
                        ) {
                            // Theme preview dots
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(16.dp).clip(CircleShape).background(themeColors.first).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                                Box(Modifier.size(16.dp).clip(CircleShape).background(themeColors.second))
                                Box(Modifier.size(16.dp).clip(CircleShape).background(themeColors.third))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = theme.label,
                                color = if (isSelected) primary else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = primary,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                Text("Board: ${PrefsManager.boardSize}x${PrefsManager.boardSize}", color = textSecondary, fontSize = 13.sp)
                Text("AI Difficulty: ${AppConfig.features.ai_difficulty}", color = textSecondary, fontSize = 13.sp)
                Text("First Player: ${PrefsManager.firstPlayer}", color = textSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound & Haptics
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Sound & Haptics", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                var soundOn by remember { mutableStateOf(PrefsManager.soundEnabled) }
                var vibrateOn by remember { mutableStateOf(PrefsManager.vibrateEnabled) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects", color = textPrimary, fontSize = 14.sp)
                    Switch(
                        checked = soundOn,
                        onCheckedChange = {
                            soundOn = it
                            PrefsManager.soundEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primary,
                            checkedTrackColor = primary.copy(alpha = 0.3f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration", color = textPrimary, fontSize = 14.sp)
                    Switch(
                        checked = vibrateOn,
                        onCheckedChange = {
                            vibrateOn = it
                            PrefsManager.vibrateEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primary,
                            checkedTrackColor = primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Settings
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Game Settings", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Board size
                Text("Board Size", color = textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                var selectedSize by remember { mutableStateOf(PrefsManager.boardSize) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(3, 4, 5).forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) primary.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) primary else textSecondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedSize = size
                                    PrefsManager.boardSize = size
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${size}x${size}",
                                color = if (isSelected) primary else textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // First player
                Text("First Player", color = textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                var selectedPlayer by remember { mutableStateOf(PrefsManager.firstPlayer) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("X" to primary, "O" to secondary).forEach { (player, color) ->
                        val isSelected = selectedPlayer == player
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else textSecondary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedPlayer = player
                                    PrefsManager.firstPlayer = player
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player,
                                color = if (isSelected) color else textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Markers
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Custom Markers", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Player X marker
                Text("Player X Marker", color = textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                var selectedX by remember { mutableStateOf(MarkerManager.markerX) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarkerManager.emojiOptions.take(10).forEach { emoji ->
                        val isSelected = selectedX == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) primary.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) primary else textSecondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedX = emoji
                                    MarkerManager.markerX = emoji
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player O marker
                Text("Player O Marker", color = textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                var selectedO by remember { mutableStateOf(MarkerManager.markerO) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarkerManager.emojiOptions.drop(10).forEach { emoji ->
                        val isSelected = selectedO == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) secondary.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) secondary else textSecondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedO = emoji
                                    MarkerManager.markerO = emoji
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // App info
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("About", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tic Tac Toe — Neon Edition", color = textSecondary, fontSize = 13.sp)
                val version = try {
                    val pInfo = com.tictactoe.TicTacToeApp.instance.packageManager.getPackageInfo(com.tictactoe.TicTacToeApp.instance.packageName, 0)
                    "v${pInfo.versionName}"
                } catch (_: Exception) { "v1.23" }
                Text("Version $version", color = textSecondary, fontSize = 13.sp)
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
