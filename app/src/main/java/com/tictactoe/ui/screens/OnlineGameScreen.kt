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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import com.tictactoe.game.GameState
import com.tictactoe.multiplayer.MultiplayerManager
import com.tictactoe.multiplayer.OnlineGame
import com.tictactoe.ui.components.GameBoard
import com.tictactoe.ui.components.NeonButton
import com.tictactoe.ui.components.WinnerCelebration

@Composable
fun OnlineGameScreen(
    roomCode: String,
    isCreator: Boolean,
    onBack: () -> Unit
) {
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    val game by MultiplayerManager.observeRoom(roomCode)
        .collectAsState(initial = OnlineGame(roomCode = roomCode))

    val amX = MultiplayerManager.amIPlayerX(game)
    val mySymbol = if (amX) "X" else "O"
    val isMyTurn = game.currentPlayer == mySymbol && game.status == "playing"

    var showCelebration by remember { mutableStateOf(false) }
    var lastStatus by remember { mutableStateOf("waiting") }

    // Trigger celebration on tournament finish
    if (game.status == "tournament_finished" && lastStatus != "tournament_finished") {
        showCelebration = true
    }
    lastStatus = game.status

    val boardSize = game.boardSize
    val gameState = remember(game.board, game.winner, game.isDraw) {
        var state = GameState(boardSize = boardSize, winLength = if (boardSize == 3) 3 else 4)
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cell = game.board.getOrNull(r)?.getOrNull(c) ?: ""
                if (cell.isNotEmpty()) {
                    state = state.makeMove(r, c)
                }
            }
        }
        state
    }

    val statusText = when {
        game.status == "waiting" -> "Waiting for opponent..."
        game.status == "tournament_finished" -> {
            val winner = when {
                game.scoreX > game.scoreO -> if (amX) "You Win!" else "You Lose!"
                game.scoreO > game.scoreX -> if (!amX) "You Win!" else "You Lose!"
                else -> "Tied!"
            }
            winner
        }
        game.status == "round_finished" -> {
            val roundWinner = game.winner
            when {
                roundWinner == mySymbol -> "You win this round!"
                game.isDraw -> "Round draw!"
                else -> "Opponent wins this round!"
            }
        }
        isMyTurn -> "Your Turn ($mySymbol)"
        else -> "Opponent's Turn"
    }

    val statusColor = when {
        game.status == "tournament_finished" -> {
            when {
                game.scoreX > game.scoreO -> if (amX) primary else secondary
                game.scoreO > game.scoreX -> if (!amX) primary else secondary
                else -> textSecondary
            }
        }
        game.status == "round_finished" -> {
            val roundWinner = game.winner
            when {
                roundWinner == mySymbol -> primary
                game.isDraw -> textSecondary
                else -> secondary
            }
        }
        isMyTurn -> primary
        else -> secondary
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = {
                if (isCreator) MultiplayerManager.deleteRoom(roomCode)
                onBack()
            },
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
            // Room code
            Text(
                text = "Room: $roomCode",
                color = textSecondary,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Round info
            Text(
                text = "Round ${game.currentRound} of ${game.totalRounds}",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (amX) "You (X)" else "Opponent (X)",
                        color = primary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${game.scoreX}",
                        color = primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = " — ",
                    color = textSecondary,
                    fontSize = 24.sp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!amX) "You (O)" else "Opponent (O)",
                        color = secondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${game.scoreO}",
                        color = secondary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Board
            GameBoard(
                gameState = gameState,
                onCellClick = { row, col ->
                    if (isMyTurn && game.status == "playing") {
                        MultiplayerManager.makeMove(roomCode, row, col)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            AnimatedVisibility(
                visible = game.status == "round_finished",
                enter = scaleIn() + fadeIn()
            ) {
                NeonButton(
                    text = if (isCreator) "NEXT ROUND" else "WAITING FOR HOST...",
                    onClick = {
                        if (isCreator) MultiplayerManager.nextRound(roomCode)
                    },
                    color = primary,
                    enabled = isCreator
                )
            }

            AnimatedVisibility(
                visible = game.status == "tournament_finished",
                enter = scaleIn() + fadeIn()
            ) {
                NeonButton(
                    text = "BACK TO LOBBY",
                    onClick = {
                        if (isCreator) MultiplayerManager.deleteRoom(roomCode)
                        onBack()
                    },
                    color = primary
                )
            }
        }

        if (showCelebration && game.status == "tournament_finished") {
            val winner = when {
                game.scoreX > game.scoreO -> "X"
                game.scoreO > game.scoreX -> "O"
                else -> ""
            }
            WinnerCelebration(
                winner = winner,
                isDraw = game.scoreX == game.scoreO,
                onDismiss = { showCelebration = false }
            )
        }
    }
}
