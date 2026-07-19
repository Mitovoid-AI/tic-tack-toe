package com.tictactoe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import com.tictactoe.data.local.GameResult
import com.tictactoe.game.GameState
import com.tictactoe.ui.components.GameBoard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReplayScreen(
    gameResult: GameResult,
    onBack: () -> Unit
) {
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    // Parse move history
    val moves = remember {
        val raw = gameResult.moveHistory
            .removePrefix("[").removeSuffix("]")
            .replace("\"", "")
        if (raw.isBlank()) emptyList()
        else raw.split(",").mapNotNull {
            val parts = it.trim().split(",")
            if (parts.size == 2) {
                parts[0].trim().toIntOrNull()?.let { r ->
                    parts[1].trim().toIntOrNull()?.let { c -> r to c }
                }
            } else null
        }
    }

    var currentStep by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Build board state for current step
    val currentState = remember(currentStep) {
        var state = GameState(boardSize = gameResult.boardSize, winLength = if (gameResult.boardSize == 3) 3 else 4)
        for (i in 0 until currentStep.coerceAtMost(moves.size)) {
            state = state.makeMove(moves[i].first, moves[i].second)
        }
        state
    }

    // Auto-play
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && currentStep < moves.size) {
                delay(800)
                currentStep++
            }
            isPlaying = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textSecondary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Replay",
                color = primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${gameResult.mode.uppercase()} — ${gameResult.winner.uppercase()}",
                color = textSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Move ${currentStep} / ${moves.size}",
                color = textPrimary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Board
            GameBoard(
                gameState = currentState,
                onCellClick = { _, _ -> },  // No interaction during replay
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls
            AnimatedVisibility(visible = moves.isNotEmpty(), enter = fadeIn()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = {
                            isPlaying = false
                            currentStep = (currentStep - 1).coerceAtLeast(0)
                        },
                        enabled = currentStep > 0
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (currentStep > 0) primary else textSecondary.copy(alpha = 0.3f)
                        )
                    }

                    // Play/Pause
                    IconButton(onClick = {
                        if (currentStep >= moves.size) currentStep = 0
                        isPlaying = !isPlaying
                    }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = primary,
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = {
                            isPlaying = false
                            currentStep = (currentStep + 1).coerceAtMost(moves.size)
                        },
                        enabled = currentStep < moves.size
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (currentStep < moves.size) primary else textSecondary.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
