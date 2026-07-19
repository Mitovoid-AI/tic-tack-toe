package com.tictactoe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert
    suspend fun insertResult(result: GameResult)

    @Query("SELECT * FROM game_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentGames(limit: Int = 20): Flow<List<GameResult>>

    @Query("SELECT COUNT(*) FROM game_results WHERE mode = :mode AND winner = 'X'")
    fun getWins(mode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_results WHERE mode = :mode AND winner = 'O'")
    fun getLosses(mode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_results WHERE mode = :mode AND winner = 'draw'")
    fun getDraws(mode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_results")
    fun getTotalGames(): Flow<Int>

    @Query("SELECT * FROM game_results WHERE id = :id")
    suspend fun getById(id: Long): GameResult?

    @Query("DELETE FROM game_results")
    suspend fun clearAll()
}
