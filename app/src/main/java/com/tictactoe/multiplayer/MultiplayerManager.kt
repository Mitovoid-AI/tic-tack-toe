package com.tictactoe.multiplayer

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.random.Random

data class OnlineGame(
    val roomCode: String = "",
    val boardSize: Int = 3,
    val board: List<List<String>> = emptyList(),
    val currentPlayer: String = "X",
    val playerX: String = "",
    val playerO: String = "",
    val winner: String = "",
    val isDraw: Boolean = false,
    val status: String = "waiting",   // waiting, playing, round_finished, tournament_finished
    val totalRounds: Int = 1,
    val currentRound: Int = 1,
    val scoreX: Int = 0,
    val scoreO: Int = 0
)

object MultiplayerManager {

    private val database: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance("https://tic-tac-toe-1c612-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (_: Exception) {
        null
    }

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private var playerId: String = Random.nextLong().toString(16)

    fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun createRoom(boardSize: Int, totalRounds: Int, callback: (String) -> Unit) {
        try {
            val db = database ?: run { callback(""); return }
            val roomCode = generateRoomCode()
            val board = List(boardSize) { List(boardSize) { "" } }

            val game = mapOf(
                "roomCode" to roomCode,
                "boardSize" to boardSize,
                "board" to board.flatten().joinToString(","),
                "currentPlayer" to "X",
                "playerX" to playerId,
                "playerO" to "",
                "winner" to "",
                "isDraw" to false,
                "status" to "waiting",
                "totalRounds" to totalRounds,
                "currentRound" to 1,
                "scoreX" to 0,
                "scoreO" to 0
            )

            db.getReference("rooms").child(roomCode).setValue(game)
                .addOnSuccessListener { callback(roomCode) }
                .addOnFailureListener { callback("") }
        } catch (_: Exception) {
            callback("")
        }
    }

    fun joinRoom(roomCode: String, callback: (Boolean) -> Unit) {
        try {
            val db = database ?: run { callback(false); return }
            val ref = db.getReference("rooms").child(roomCode)

            ref.child("playerO").get().addOnSuccessListener { snapshot ->
                try {
                    val current = snapshot.getValue(String::class.java) ?: ""
                    if (current.isEmpty()) {
                        ref.child("playerO").setValue(playerId)
                        ref.child("status").setValue("playing")
                        callback(true)
                    } else {
                        callback(false)
                    }
                } catch (_: Exception) {
                    callback(false)
                }
            }.addOnFailureListener { callback(false) }
        } catch (_: Exception) {
            callback(false)
        }
    }

    fun makeMove(roomCode: String, row: Int, col: Int) {
        try {
            val db = database ?: return
            val ref = db.getReference("rooms").child(roomCode)

            ref.get().addOnSuccessListener { snapshot ->
                try {
                    val boardStr = snapshot.child("board").getValue(String::class.java) ?: return@addOnSuccessListener
                    val boardSize = snapshot.child("boardSize").getValue(Int::class.java) ?: 3
                    val currentPlayer = snapshot.child("currentPlayer").getValue(String::class.java) ?: "X"
                    val currentStatus = snapshot.child("status").getValue(String::class.java) ?: ""

                    if (currentStatus != "playing") return@addOnSuccessListener

                    val cells = boardStr.split(",").toMutableList()
                    if (cells.size != boardSize * boardSize) return@addOnSuccessListener

                    val index = row * boardSize + col
                    if (index !in cells.indices) return@addOnSuccessListener
                    if (cells[index].isNotEmpty()) return@addOnSuccessListener

                    cells[index] = currentPlayer
                    val nextPlayer = if (currentPlayer == "X") "O" else "X"

                    val board = List(boardSize) { r -> List(boardSize) { c -> cells[r * boardSize + c] } }
                    val hasWon = checkWin(board, row, col, currentPlayer, boardSize)
                    val isFull = cells.none { it.isEmpty() }
                    val isDraw = !hasWon && isFull

                    val roundOver = hasWon || isDraw

                    // Get current scores
                    val scoreX = snapshot.child("scoreX").getValue(Int::class.java) ?: 0
                    val scoreO = snapshot.child("scoreO").getValue(Int::class.java) ?: 0
                    val totalRounds = snapshot.child("totalRounds").getValue(Int::class.java) ?: 1
                    val currentRound = snapshot.child("currentRound").getValue(Int::class.java) ?: 1

                    // Calculate new scores
                    val newScoreX = if (hasWon && currentPlayer == "X") scoreX + 1 else scoreX
                    val newScoreO = if (hasWon && currentPlayer == "O") scoreO + 1 else scoreO

                    // Check if tournament is over
                    val winsNeeded = (totalRounds / 2) + 1
                    val tournamentOver = roundOver && (
                        newScoreX >= winsNeeded ||
                        newScoreO >= winsNeeded ||
                        currentRound >= totalRounds
                    )

                    val newStatus = when {
                        tournamentOver -> "tournament_finished"
                        roundOver -> "round_finished"
                        else -> "playing"
                    }

                    val updates = mutableMapOf<String, Any>(
                        "board" to cells.joinToString(","),
                        "currentPlayer" to if (roundOver) currentPlayer else nextPlayer,
                        "winner" to if (hasWon) currentPlayer else "",
                        "isDraw" to isDraw,
                        "status" to newStatus,
                        "scoreX" to newScoreX,
                        "scoreO" to newScoreO
                    )

                    ref.updateChildren(updates)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    fun nextRound(roomCode: String) {
        try {
            val db = database ?: return
            val ref = db.getReference("rooms").child(roomCode)

            ref.get().addOnSuccessListener { snapshot ->
                try {
                    val boardSize = snapshot.child("boardSize").getValue(Int::class.java) ?: 3
                    val currentRound = snapshot.child("currentRound").getValue(Int::class.java) ?: 1
                    val board = List(boardSize) { List(boardSize) { "" } }

                    val updates = mapOf(
                        "board" to board.flatten().joinToString(","),
                        "currentPlayer" to "X",
                        "winner" to "",
                        "isDraw" to false,
                        "status" to "playing",
                        "currentRound" to (currentRound + 1)
                    )

                    ref.updateChildren(updates)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    fun observeRoom(roomCode: String): Flow<OnlineGame> = callbackFlow {
        val db = database ?: run { close(); return@callbackFlow }
        val ref = db.getReference("rooms").child(roomCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val boardStr = snapshot.child("board").getValue(String::class.java) ?: ""
                    val boardSize = snapshot.child("boardSize").getValue(Int::class.java) ?: 3
                    val cells = boardStr.split(",")
                    val board = List(boardSize) { r ->
                        List(boardSize) { c ->
                            cells.getOrElse(r * boardSize + c) { "" }
                        }
                    }

                    val game = OnlineGame(
                        roomCode = roomCode,
                        boardSize = boardSize,
                        board = board,
                        currentPlayer = snapshot.child("currentPlayer").getValue(String::class.java) ?: "X",
                        playerX = snapshot.child("playerX").getValue(String::class.java) ?: "",
                        playerO = snapshot.child("playerO").getValue(String::class.java) ?: "",
                        winner = snapshot.child("winner").getValue(String::class.java) ?: "",
                        isDraw = snapshot.child("isDraw").getValue(Boolean::class.java) ?: false,
                        status = snapshot.child("status").getValue(String::class.java) ?: "waiting",
                        totalRounds = snapshot.child("totalRounds").getValue(Int::class.java) ?: 1,
                        currentRound = snapshot.child("currentRound").getValue(Int::class.java) ?: 1,
                        scoreX = snapshot.child("scoreX").getValue(Int::class.java) ?: 0,
                        scoreO = snapshot.child("scoreO").getValue(Int::class.java) ?: 0
                    )
                    trySend(game)
                } catch (_: Exception) {}
            }

            override fun onCancelled(error: DatabaseError) {
                close()
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getPlayerId() = playerId

    fun amIPlayerX(game: OnlineGame) = game.playerX == playerId

    private fun checkWin(board: List<List<String>>, row: Int, col: Int, player: String, boardSize: Int): Boolean {
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        for ((dr, dc) in directions) {
            var count = 1
            var r = row + dr; var c = col + dc
            while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == player) { count++; r += dr; c += dc }
            r = row - dr; c = col - dc
            while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == player) { count++; r -= dr; c -= dc }
            if (count >= 3) return true
        }
        return false
    }

    fun deleteRoom(roomCode: String) {
        try {
            database?.getReference("rooms")?.child(roomCode)?.removeValue()
        } catch (_: Exception) {}
    }
}
