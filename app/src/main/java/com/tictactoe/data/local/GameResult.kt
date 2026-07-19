package com.tictactoe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String,          // "pvp" or "ai"
    val winner: String,        // "X", "O", or "draw"
    val moves: Int,            // total moves in the game
    val boardSize: Int = 3
)
