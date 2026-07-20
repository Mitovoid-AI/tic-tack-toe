package com.tictactoe.game

data class GameState(
    val boardSize: Int = 3,
    val winLength: Int = 3,
    val board: List<List<String>> = List(boardSize) { List(boardSize) { "" } },
    val currentPlayer: String = "X",
    val winner: String? = null,
    val winningCells: List<Pair<Int, Int>> = emptyList(),
    val isDraw: Boolean = false,
    val moveCount: Int = 0,
    val isGameOver: Boolean = false
) {
    fun makeMove(row: Int, col: Int): GameState {
        if (row !in 0 until boardSize || col !in 0 until boardSize) return this
        if (board[row][col].isNotEmpty() || isGameOver) return this

        val newBoard = board.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) currentPlayer else cell
            }
        }

        val newMoveCount = moveCount + 1
        val winCells = checkWin(newBoard, row, col, currentPlayer)
        val hasWon = winCells.isNotEmpty()
        val isFull = newMoveCount >= boardSize * boardSize
        val draw = !hasWon && isFull

        return copy(
            board = newBoard,
            currentPlayer = if (hasWon || draw) currentPlayer else if (currentPlayer == "X") "O" else "X",
            winner = if (hasWon) currentPlayer else null,
            winningCells = winCells,
            isDraw = draw,
            moveCount = newMoveCount,
            isGameOver = hasWon || draw
        )
    }

    private fun checkWin(board: List<List<String>>, row: Int, col: Int, player: String): List<Pair<Int, Int>> {
        val directions = listOf(
            0 to 1,   // horizontal
            1 to 0,   // vertical
            1 to 1,   // diagonal
            1 to -1   // anti-diagonal
        )

        for ((dr, dc) in directions) {
            val cells = mutableListOf(row to col)

            // Forward
            var r = row + dr
            var c = col + dc
            while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == player) {
                cells.add(r to c)
                r += dr
                c += dc
            }

            // Backward
            r = row - dr
            c = col - dc
            while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == player) {
                cells.add(r to c)
                r -= dr
                c -= dc
            }

            if (cells.size >= winLength) return cells.take(winLength)
        }

        return emptyList()
    }

    fun getAvailableMoves(): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (board[r][c].isEmpty()) moves.add(r to c)
            }
        }
        return moves
    }

    fun reset(): GameState = GameState(boardSize = boardSize, winLength = winLength)

    companion object {
        fun fromBoard(board: List<List<String>>, boardSize: Int, winLength: Int, currentPlayer: String): GameState {
            var state = GameState(boardSize = boardSize, winLength = winLength, currentPlayer = currentPlayer)
            // Set the board directly without replaying moves
            state = state.copy(
                board = board,
                moveCount = board.flatten().count { it.isNotEmpty() }
            )
            // Check if there's already a winner by scanning the board
            state = state.checkBoardForWinner()
            return state
        }
    }

    private fun checkBoardForWinner(): GameState {
        // Check all cells for a win
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val player = board[r][c]
                if (player.isNotEmpty()) {
                    val winCells = checkWin(board, r, c, player)
                    if (winCells.isNotEmpty()) {
                        return copy(
                            winner = player,
                            winningCells = winCells,
                            isGameOver = true,
                            isDraw = false
                        )
                    }
                }
            }
        }
        // Check for draw
        val isFull = board.flatten().none { it.isEmpty() }
        if (isFull) {
            return copy(isDraw = true, isGameOver = true)
        }
        return this
    }
}
