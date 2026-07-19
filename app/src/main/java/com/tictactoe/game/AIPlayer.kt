package com.tictactoe.game

import kotlin.random.Random

object AIPlayer {

    fun getMove(state: GameState, difficulty: String): Pair<Int, Int>? {
        val available = state.getAvailableMoves()
        if (available.isEmpty()) return null

        return when (difficulty) {
            "easy" -> easyMove(available)
            "hard" -> hardMove(state, available)
            else -> mediumMove(state, available)
        }
    }

    private fun easyMove(available: List<Pair<Int, Int>>): Pair<Int, Int> {
        return available[Random.nextInt(available.size)]
    }

    private fun mediumMove(state: GameState, available: List<Pair<Int, Int>>): Pair<Int, Int> {
        // Try to win
        for ((r, c) in available) {
            val test = state.makeMove(r, c)
            if (test.winner == "O") return r to c
        }

        // Block opponent
        for ((r, c) in available) {
            val test = state.makeMove(r, c)
            if (test.winner == "X") return r to c
        }

        // Take center if available
        val center = state.boardSize / 2
        if (available.contains(center to center)) return center to center

        // Random
        return available[Random.nextInt(available.size)]
    }

    private fun hardMove(state: GameState, available: List<Pair<Int, Int>>): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove = available[0]

        for ((r, c) in available) {
            val newState = state.makeMove(r, c)
            val score = minimax(newState, depth = 0, isMaximizing = false, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = r to c
            }
        }

        return bestMove
    }

    private fun minimax(state: GameState, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int {
        if (state.winner == "O") return 10 - depth
        if (state.winner == "X") return depth - 10
        if (state.isDraw) return 0

        var a = alpha
        var b = beta

        if (isMaximizing) {
            var maxScore = Int.MIN_VALUE
            for ((r, c) in state.getAvailableMoves()) {
                val score = minimax(state.makeMove(r, c), depth + 1, false, a, b)
                maxScore = maxOf(maxScore, score)
                a = maxOf(a, score)
                if (b <= a) break
            }
            return maxScore
        } else {
            var minScore = Int.MAX_VALUE
            for ((r, c) in state.getAvailableMoves()) {
                val score = minimax(state.makeMove(r, c), depth + 1, true, a, b)
                minScore = minOf(minScore, score)
                b = minOf(b, score)
                if (b <= a) break
            }
            return minScore
        }
    }
}
