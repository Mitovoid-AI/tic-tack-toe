package com.tictactoe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig

@Composable
fun ScoreBoard(
    scoreX: Int,
    scoreO: Int,
    scoreDraw: Int,
    mode: String,
    modifier: Modifier = Modifier
) {
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreItem("X ${if (mode == "ai") "(You)" else ""}", scoreX, primary)
        ScoreItem("Draw", scoreDraw, textSecondary)
        ScoreItem("O ${if (mode == "ai") "(AI)" else ""}", scoreO, secondary)
    }
}

@Composable
private fun ScoreItem(label: String, score: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = score.toString(),
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
