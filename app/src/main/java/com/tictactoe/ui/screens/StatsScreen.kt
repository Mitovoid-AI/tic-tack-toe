package com.tictactoe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
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
import com.tictactoe.data.local.GameResult
import com.tictactoe.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val repo = TicTacToeApp.instance.gameRepository
    val totalGames by repo.getTotalGames().collectAsState(initial = 0)
    val pvpWins by repo.getWins("pvp").collectAsState(initial = 0)
    val pvpLosses by repo.getLosses("pvp").collectAsState(initial = 0)
    val pvpDraws by repo.getDraws("pvp").collectAsState(initial = 0)
    val aiWins by repo.getWins("ai").collectAsState(initial = 0)
    val aiLosses by repo.getLosses("ai").collectAsState(initial = 0)
    val aiDraws by repo.getDraws("ai").collectAsState(initial = 0)
    val recentGames by repo.getRecentGames().collectAsState(initial = emptyList())

    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
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
                text = "Statistics",
                color = primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total
        Text(
            text = "Total Games: $totalGames",
            color = textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("PvP", color = primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("W: $pvpWins", color = textPrimary)
                    Text("L: $pvpLosses", color = textPrimary)
                    Text("D: $pvpDraws", color = textSecondary)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("vs AI", color = secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("W: $aiWins", color = textPrimary)
                    Text("L: $aiLosses", color = textPrimary)
                    Text("D: $aiDraws", color = textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent games
        Text(
            text = "Recent Games",
            color = textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recentGames.isEmpty()) {
            Text(
                text = "No games played yet",
                color = textSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn {
                items(recentGames) { game ->
                    GameHistoryItem(game)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun GameHistoryItem(game: GameResult) {
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()
    val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(game.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${game.mode.uppercase()} — ${game.winner.uppercase()}",
                color = textPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(text = date, color = textSecondary, fontSize = 12.sp)
        }
        Text(
            text = "${game.moves} moves",
            color = textSecondary,
            fontSize = 13.sp
        )
    }
}
