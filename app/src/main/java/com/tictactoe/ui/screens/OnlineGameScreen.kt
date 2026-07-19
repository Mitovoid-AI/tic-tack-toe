package com.tictactoe.ui.screens

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

    // Trigger celebration on game end
    if (game.status == "finished" && lastStatus == "playing") {
        showCelebration = true
    }
    lastStatus = game.status

    // Convert online board to GameState for GameBoard composable
    val boardSize = game.boardSize
    val gameState = remember(game) {
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
        game.isDraw -> "It's a Draw!"
        game.winner == mySymbol -> "You Win!"
        game.winner.isNotEmpty() -> "You Lose!"
        isMyTurn -> "Your Turn ($mySymbol)"
        else -> "Opponent's Turn"
    }

    val statusColor = when {
        game.status == "waiting" -> textSecondary
        game.winner == mySymbol -> primary
        game.winner.isNotEmpty() -> secondary
        game.isDraw -> textSecondary
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
            Text(
                text = "Room: $roomCode",
                color = textSecondary,
                fontSize = 14.sp,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You are $mySymbol",
                color = if (amX) primary else secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            if (game.status == "finished") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NeonButton(text = "BACK", onClick = {
                        if (isCreator) MultiplayerManager.deleteRoom(roomCode)
                        onBack()
                    }, color = primary)
                }
            }
        }

        if (showCelebration && game.status == "finished") {
            WinnerCelebration(
                winner = game.winner,
                isDraw = game.isDraw,
                onDismiss = { showCelebration = false }
            )
        }
    }
}
