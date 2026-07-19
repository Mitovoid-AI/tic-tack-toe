package com.tictactoe.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.TicTacToeApp
import com.tictactoe.config.AppConfig
import com.tictactoe.util.HapticManager
import com.tictactoe.util.PrefsManager
import com.tictactoe.util.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameUiState(
    val gameState: GameState = GameState(),
    val mode: String = "pvp",
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
    private val moveList = mutableListOf<Pair<Int, Int>>()
    private var aiSymbol = "O"

    fun startGame(mode: String) {
        val boardSize = PrefsManager.boardSize
        val winLength = if (boardSize == 3) 3 else 4
        val firstPlayer = PrefsManager.firstPlayer
        aiSymbol = if (firstPlayer == "X") "O" else "X"
        stateHistory.clear()
        moveList.clear()
        _uiState.update {
            it.copy(
                mode = mode,
                gameState = GameState(boardSize = boardSize, winLength = winLength, currentPlayer = firstPlayer),
                scoreX = 0,
                scoreO = 0,
                scoreDraw = 0,
                canUndo = false,
                isAiThinking = false
            )
        }
    }

    fun onCellClick(row: Int, col: Int) {
        val current = _uiState.value
        if (current.gameState.isGameOver || current.isAiThinking) return

        // Bug fix: validate move BEFORE modifying history
        val newState = current.gameState.makeMove(row, col)
        if (newState === current.gameState) return // No-op (cell occupied or out of bounds)

        stateHistory.add(current.gameState)
        moveList.add(row to col)
        _uiState.update { it.copy(gameState = newState, canUndo = true) }

        try { SoundManager.playTap() } catch (_: Exception) {}
        try { HapticManager.lightTap() } catch (_: Exception) {}

        if (newState.isGameOver) {
            onGameEnd(newState)
            return
        }

        // AI turn: trigger for whichever symbol the AI plays
        if (current.mode == "ai" && newState.currentPlayer == aiSymbol) {
            triggerAiMove()
        }
    }

    fun undo() {
        // Bug fix: guard against undo during AI thinking
        if (_uiState.value.isAiThinking) return
        if (stateHistory.isEmpty()) return

        val mode = _uiState.value.mode
        val stepsBack = if (mode == "ai" && stateHistory.size >= 2) 2 else 1

        repeat(stepsBack) {
            if (stateHistory.isNotEmpty()) {
                stateHistory.removeAt(stateHistory.lastIndex)
                if (moveList.isNotEmpty()) moveList.removeAt(moveList.lastIndex)
            }
        }

        // Bug fix: restore the correct state (the one on top after removals)
        val boardSize = _uiState.value.gameState.boardSize
        val winLength = _uiState.value.gameState.winLength
        val restoredState = stateHistory.lastOrNull()
            ?: GameState(boardSize = boardSize, winLength = winLength)
        _uiState.update { it.copy(gameState = restoredState, canUndo = stateHistory.isNotEmpty()) }
    }

    private fun triggerAiMove() {
        _uiState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch {
            delay(300)

            // Bug fix: check state hasn't changed during delay
            val currentState = _uiState.value
            if (currentState.gameState.isGameOver || !currentState.isAiThinking) return@launch

            val state = currentState.gameState
            val difficulty = AppConfig.features.ai_difficulty

            // Bug fix: run AI on background thread to avoid blocking UI on 4x4/5x5
            val move = withContext(Dispatchers.Default) {
                AIPlayer.getMove(state, difficulty, aiSymbol)
            }

            if (move != null) {
                // Bug fix: add AI move to history BEFORE applying it
                val preAiState = state
                stateHistory.add(preAiState)
                moveList.add(move.first to move.second)

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
        try {
            if (state.isDraw) {
                SoundManager.playDraw()
                HapticManager.drawVibration()
            } else {
                SoundManager.playWin()
                HapticManager.winVibration()
            }
        } catch (_: Exception) {}

        _uiState.update {
            it.copy(
                scoreX = if (winner == "X") it.scoreX + 1 else it.scoreX,
                scoreO = if (winner == "O") it.scoreO + 1 else it.scoreO,
                scoreDraw = if (state.isDraw) it.scoreDraw + 1 else it.scoreDraw
            )
        }

        val moveHistoryJson = moveList.joinToString(",") { "\"${it.first},${it.second}\"" }
        val moveHistoryStr = "[$moveHistoryJson]"

        viewModelScope.launch {
            try {
                repo.saveResult(
                    mode = _uiState.value.mode,
                    winner = winner ?: "draw",
                    moves = state.moveCount,
                    boardSize = state.boardSize,
                    moveHistory = moveHistoryStr
                )
            } catch (_: Exception) {}
        }
    }

    fun resetBoard() {
        stateHistory.clear()
        moveList.clear()
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
