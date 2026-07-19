package com.tictactoe.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.TicTacToeApp
import com.tictactoe.config.AppConfig
import com.tictactoe.util.HapticManager
import com.tictactoe.util.PrefsManager
import com.tictactoe.util.SoundManager
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
    val isAiThinking: Boolean = false,
    val canUndo: Boolean = false
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val repo = TicTacToeApp.instance.gameRepository
    private val stateHistory = mutableListOf<GameState>()

    fun startGame(mode: String) {
        val boardSize = PrefsManager.boardSize
        val winLength = if (boardSize == 3) 3 else if (boardSize == 4) 4 else 4
        val firstPlayer = PrefsManager.firstPlayer
        stateHistory.clear()
        _uiState.update {
            it.copy(
                mode = mode,
                gameState = GameState(boardSize = boardSize, winLength = winLength, currentPlayer = firstPlayer),
                scoreX = 0,
                scoreO = 0,
                scoreDraw = 0,
                canUndo = false
            )
        }
    }

    fun onCellClick(row: Int, col: Int) {
        val current = _uiState.value
        if (current.gameState.isGameOver || current.isAiThinking) return

        stateHistory.add(current.gameState)
        val newState = current.gameState.makeMove(row, col)
        _uiState.update { it.copy(gameState = newState, canUndo = stateHistory.isNotEmpty()) }
        SoundManager.playTap()
        HapticManager.lightTap()

        if (newState.isGameOver) {
            onGameEnd(newState)
            return
        }

        // AI turn
        if (current.mode == "ai" && newState.currentPlayer == "O") {
            triggerAiMove()
        }
    }

    fun undo() {
        if (stateHistory.isEmpty()) return
        // In AI mode, undo twice (your move + AI move)
        val stepsBack = if (_uiState.value.mode == "ai" && stateHistory.size >= 2) 2 else 1
        repeat(stepsBack) {
            if (stateHistory.isNotEmpty()) {
                val previousState = stateHistory.removeAt(stateHistory.lastIndex)
                _uiState.update { it.copy(gameState = previousState, canUndo = stateHistory.isNotEmpty()) }
            }
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
        if (state.isDraw) {
            SoundManager.playDraw()
            HapticManager.drawVibration()
        } else {
            SoundManager.playWin()
            HapticManager.winVibration()
        }

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
        stateHistory.clear()
        _uiState.update {
            it.copy(
                gameState = it.gameState.reset(),
                isAiThinking = false,
                canUndo = false
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
