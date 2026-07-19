package com.tictactoe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.TicTacToeApp
import com.tictactoe.config.AppConfig
import com.tictactoe.ui.components.GlassCard

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val repo = TicTacToeApp.instance.gameRepository
    val totalGames by repo.getTotalGames().collectAsState(initial = 0)
    val pvpWins by repo.getWins("pvp").collectAsState(initial = 0)
    val pvpLosses by repo.getLosses("pvp").collectAsState(initial = 0)
    val pvpDraws by repo.getDraws("pvp").collectAsState(initial = 0)
    val aiWins by repo.getWins("ai").collectAsState(initial = 0)
    val aiLosses by repo.getLosses("ai").collectAsState(initial = 0)
    val aiDraws by repo.getDraws("ai").collectAsState(initial = 0)

    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val accent = AppConfig.accentColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    val totalWins = pvpWins + aiWins
    val totalLosses = pvpLosses + aiLosses
    val totalDraws = pvpDraws + aiDraws
    val winRate = if (totalGames > 0) (totalWins * 100 / totalGames) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textSecondary)
            }
            Text(
                text = "Leaderboard",
                color = primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Overall stats
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("YOUR STATS", color = textSecondary, fontSize = 12.sp, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$totalGames",
                    color = primary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Games Played", color = textSecondary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn("$totalWins", "Wins", primary)
                    StatColumn("$totalLosses", "Losses", secondary)
                    StatColumn("$totalDraws", "Draws", textSecondary)
                    StatColumn("$winRate%", "Win Rate", accent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rank card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("YOUR RANK", color = textSecondary, fontSize = 12.sp, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val rank = when {
                    totalGames >= 100 -> "\uD83E\uDD47"  // Gold
                    totalGames >= 50 -> "\uD83E\uDD48"   // Silver
                    totalGames >= 20 -> "\uD83E\uDD49"   // Bronze
                    totalGames >= 10 -> "\u2B50"          // Star
                    else -> "\uD83C\uDFB0"               // Dice
                }

                val rankName = when {
                    totalGames >= 100 -> "Legend"
                    totalGames >= 50 -> "Master"
                    totalGames >= 20 -> "Veteran"
                    totalGames >= 10 -> "Rising Star"
                    else -> "Newcomer"
                }

                Text(text = rank, fontSize = 48.sp)
                Text(
                    text = rankName,
                    color = accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val nextMilestone = when {
                    totalGames < 10 -> 10
                    totalGames < 20 -> 20
                    totalGames < 50 -> 50
                    totalGames < 100 -> 100
                    else -> null
                }

                if (nextMilestone != null) {
                    Text(
                        text = "${nextMilestone - totalGames} games to next rank",
                        color = textSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "You've reached the top!",
                        color = textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("PvP", color = primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$pvpWins W / $pvpLosses L / $pvpDraws D", color = textPrimary, fontSize = 12.sp)
                    val pvpRate = if (pvpWins + pvpLosses > 0) (pvpWins * 100 / (pvpWins + pvpLosses)) else 0
                    Text("$pvpRate% win rate", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("vs AI", color = secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$aiWins W / $aiLosses L / $aiDraws D", color = textPrimary, fontSize = 12.sp)
                    val aiRate = if (aiWins + aiLosses > 0) (aiWins * 100 / (aiWins + aiLosses)) else 0
                    Text("$aiRate% win rate", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = AppConfig.textSecondaryColor(), fontSize = 11.sp)
    }
}
