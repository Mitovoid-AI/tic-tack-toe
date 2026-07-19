package com.tictactoe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String,
    val winner: String,
    val moves: Int,
    val boardSize: Int = 3,
    val moveHistory: String = "[]"   // JSON array of "row,col" pairs
)
