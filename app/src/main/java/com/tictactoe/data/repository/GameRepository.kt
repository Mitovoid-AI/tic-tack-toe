package com.tictactoe.data.repository

import com.tictactoe.data.local.GameDao
import com.tictactoe.data.local.GameResult
import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {

    suspend fun saveResult(mode: String, winner: String, moves: Int, boardSize: Int, moveHistory: String = "[]") {
        dao.insertResult(
            GameResult(
                mode = mode,
                winner = winner,
                moves = moves,
                boardSize = boardSize,
                moveHistory = moveHistory
            )
        )
    }

    fun getRecentGames(limit: Int = 20): Flow<List<GameResult>> = dao.getRecentGames(limit)

    fun getWins(mode: String): Flow<Int> = dao.getWins(mode)
    fun getLosses(mode: String): Flow<Int> = dao.getLosses(mode)
    fun getDraws(mode: String): Flow<Int> = dao.getDraws(mode)
    fun getTotalGames(): Flow<Int> = dao.getTotalGames()

    suspend fun getById(id: Long): GameResult? = dao.getById(id)

    suspend fun clearAll() = dao.clearAll()
}
