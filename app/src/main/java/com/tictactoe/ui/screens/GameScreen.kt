package com.tictactoe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tictactoe.config.AppConfig
import com.tictactoe.game.GameViewModel
import com.tictactoe.ui.components.GameBoard
import com.tictactoe.ui.components.NeonButton
import com.tictactoe.ui.components.ScoreBoard
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    mode: String,
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    LaunchedEffect(mode) {
        viewModel.startGame(mode)
    }

    val state by viewModel.uiState.collectAsState()
    val game = state.gameState

    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()

    // Status text
    val statusText = when {
        game.isGameOver && game.winner != null -> {
            if (mode == "ai") {
                if (game.winner == "X") "You Win!" else "AI Wins!"
            } else {
                "${game.winner} Wins!"
            }
        }
        game.isDraw -> "It's a Draw!"
        state.isAiThinking -> "AI is thinking..."
        else -> {
            if (mode == "ai") {
                if (game.currentPlayer == "X") "Your Turn" else "AI's Turn"
            } else {
                "${game.currentPlayer}'s Turn"
            }
        }
    }

    val statusColor = when {
        game.isGameOver && game.winner == "X" -> primary
        game.isGameOver && game.winner == "O" -> secondary
        game.isDraw -> textSecondary
        game.currentPlayer == "X" -> primary
        else -> secondary
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Turn / Status
            AnimatedVisibility(
                visible = true,
                enter = fadeIn()
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score
            ScoreBoard(
                scoreX = state.scoreX,
                scoreO = state.scoreO,
                scoreDraw = state.scoreDraw,
                mode = mode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Board
            GameBoard(
                gameState = game,
                onCellClick = { row, col -> viewModel.onCellClick(row, col) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            AnimatedVisibility(
                visible = game.isGameOver,
                enter = scaleIn() + fadeIn()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NeonButton(text = "REMATCH", onClick = { viewModel.resetBoard() }, color = primary)
                    NeonButton(text = "RESET", onClick = { viewModel.resetScores() }, color = secondary)
                }
            }

            if (!game.isGameOver) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = "Undo",
                            tint = if (state.canUndo) textSecondary else textSecondary.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = { viewModel.resetBoard() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = textSecondary
                        )
                    }
                }
            }
        }
    }
}
