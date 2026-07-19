package com.tictactoe.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.TicTacToeApp
import com.tictactoe.config.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState = GameState(),
    val mode: String = "pvp",       // "pvp" or "ai"
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val scoreDraw: Int = 0,
    val isAiThinking: Boolean = false
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val repo = TicTacToeApp.instance.gameRepository

    fun startGame(mode: String) {
        val boardSize = AppConfig.board.size
        val winLength = AppConfig.board.win_length
        _uiState.update {
            it.copy(
                mode = mode,
                gameState = GameState(boardSize = boardSize, winLength = winLength),
                scoreX = 0,
                scoreO = 0,
                scoreDraw = 0
            )
        }
    }

    fun onCellClick(row: Int, col: Int) {
        val current = _uiState.value
        if (current.gameState.isGameOver || current.isAiThinking) return

        val newState = current.gameState.makeMove(row, col)
        _uiState.update { it.copy(gameState = newState) }

        if (newState.isGameOver) {
            onGameEnd(newState)
            return
        }

        // AI turn
        if (current.mode == "ai" && newState.currentPlayer == "O") {
            triggerAiMove()
        }
    }

    private fun triggerAiMove() {
        _uiState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch {
            delay(300) // Small delay so AI doesn't feel instant

            val state = _uiState.value.gameState
            val difficulty = AppConfig.features.ai_difficulty
            val move = AIPlayer.getMove(state, difficulty)

            if (move != null) {
                val newState = state.makeMove(move.first, move.second)
                _uiState.update { it.copy(gameState = newState, isAiThinking = false) }

                if (newState.isGameOver) {
                    onGameEnd(newState)
                }
            } else {
                _uiState.update { it.copy(isAiThinking = false) }
            }
        }
    }

    private fun onGameEnd(state: GameState) {
        val winner = state.winner
        _uiState.update {
            it.copy(
                scoreX = if (winner == "X") it.scoreX + 1 else it.scoreX,
                scoreO = if (winner == "O") it.scoreO + 1 else it.scoreO,
                scoreDraw = if (state.isDraw) it.scoreDraw + 1 else it.scoreDraw
            )
        }

        viewModelScope.launch {
            repo.saveResult(
                mode = _uiState.value.mode,
                winner = winner ?: "draw",
                moves = state.moveCount,
                boardSize = state.boardSize
            )
        }
    }

    fun resetBoard() {
        _uiState.update {
            it.copy(
                gameState = it.gameState.reset(),
                isAiThinking = false
            )
        }
    }

    fun resetScores() {
        _uiState.update {
            it.copy(scoreX = 0, scoreO = 0, scoreDraw = 0)
        }
        resetBoard()
    }
}
