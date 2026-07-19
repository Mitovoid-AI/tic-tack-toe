package com.tictactoe.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

    // Shake on draw
    var shakeTrigger by remember { mutableIntStateOf(0) }
    val shakeOffset by animateFloatAsState(
        targetValue = when {
            shakeTrigger == 0 -> 0f
            shakeTrigger % 2 == 1 -> 8f
            else -> -8f
        },
        animationSpec = tween(50),
        label = "shake"
    )

    LaunchedEffect(gameState.isDraw) {
        if (gameState.isDraw) {
            repeat(6) { i ->
                shakeTrigger = i + 1
                delay(50)
            }
            shakeTrigger = 0
        }
    }

    // Pulse glow on win
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (gameState.isGameOver && gameState.winner != null) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Win border glow
    val glowAlpha by animateFloatAsState(
        targetValue = if (gameState.isGameOver && gameState.winner != null) 0.6f else 0f,
        animationSpec = tween(500),
        label = "glow"
    )

    val winnerColor = when (gameState.winner) {
        "X" -> AppConfig.primaryColor()
        "O" -> AppConfig.secondaryColor()
        else -> Color.Transparent
    }

    val surfaceColor = AppConfig.surfaceColor()

    Box(
        modifier = modifier
            .alpha(boardAlpha)
            .offset(x = shakeOffset.dp)
            .scale(pulseScale)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor.copy(alpha = 0.3f))
            .border(
                width = if (glowAlpha > 0f) 2.dp else 1.dp,
                color = if (glowAlpha > 0f) winnerColor.copy(alpha = glowAlpha) else Color.White.copy(alpha = 0.08f),
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
