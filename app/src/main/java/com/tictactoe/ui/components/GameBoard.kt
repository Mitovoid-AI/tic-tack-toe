package com.tictactoe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tictactoe.config.AppConfig
import com.tictactoe.game.GameState
import kotlinx.coroutines.delay

@Composable
fun GameBoard(
    gameState: GameState,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Staggered reveal animation
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        revealed = true
    }
    val boardAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(500),
        label = "board_alpha"
    )

    val surfaceColor = AppConfig.surfaceColor()

    Box(
        modifier = modifier
            .alpha(boardAlpha)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            for (row in 0 until gameState.boardSize) {
                Row {
                    for (col in 0 until gameState.boardSize) {
                        CellView(
                            value = gameState.board[row][col],
                            isWinning = gameState.winningCells.contains(row to col),
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
