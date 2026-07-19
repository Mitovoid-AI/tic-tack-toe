package com.tictactoe.game

import kotlin.random.Random

object AIPlayer {

    fun getMove(state: GameState, difficulty: String, aiSymbol: String = "O"): Pair<Int, Int>? {
        val available = state.getAvailableMoves()
        if (available.isEmpty()) return null

        return when (difficulty) {
            "easy" -> easyMove(available)
            "hard" -> hardMove(state, available, aiSymbol)
            else -> mediumMove(state, available, aiSymbol)
        }
    }

    private fun easyMove(available: List<Pair<Int, Int>>): Pair<Int, Int> {
        return available[Random.nextInt(available.size)]
    }

    private fun mediumMove(state: GameState, available: List<Pair<Int, Int>>, aiSymbol: String): Pair<Int, Int> {
        val opponentSymbol = if (aiSymbol == "X") "O" else "X"

        // Try to win
        for ((r, c) in available) {
            val test = state.makeMove(r, c)
            if (test.winner == aiSymbol) return r to c
        }

        // Block opponent
        for ((r, c) in available) {
            val test = state.makeMove(r, c)
            if (test.winner == opponentSymbol) return r to c
        }

        // Take center if available
        val center = state.boardSize / 2
        if (available.contains(center to center)) return center to center

        // Random
        return available[Random.nextInt(available.size)]
    }

    private fun hardMove(state: GameState, available: List<Pair<Int, Int>>, aiSymbol: String): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove = available[0]
        val opponentSymbol = if (aiSymbol == "X") "O" else "X"

        for ((r, c) in available) {
            val newState = state.makeMove(r, c)
            val score = minimax(newState, depth = 0, isMaximizing = false, aiSymbol, opponentSymbol, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = r to c
            }
        }

        return bestMove
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        isMaximizing: Boolean,
        aiSymbol: String,
        opponentSymbol: String,
        alpha: Int,
        beta: Int
    ): Int {
        if (state.winner == aiSymbol) return 10 - depth
        if (state.winner == opponentSymbol) return depth - 10
        if (state.isDraw) return 0

        var a = alpha
        var b = beta

        if (isMaximizing) {
            var maxScore = Int.MIN_VALUE
            for ((r, c) in state.getAvailableMoves()) {
                val score = minimax(state.makeMove(r, c), depth + 1, false, aiSymbol, opponentSymbol, a, b)
                maxScore = maxOf(maxScore, score)
                a = maxOf(a, score)
                if (b <= a) break
            }
            return maxScore
        } else {
            var minScore = Int.MAX_VALUE
            for ((r, c) in state.getAvailableMoves()) {
                val score = minimax(state.makeMove(r, c), depth + 1, true, aiSymbol, opponentSymbol, a, b)
                minScore = minOf(minScore, score)
                b = minOf(b, score)
                if (b <= a) break
            }
            return minScore
        }
    }
}
