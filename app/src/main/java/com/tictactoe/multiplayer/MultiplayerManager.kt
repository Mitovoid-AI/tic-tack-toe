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
    val status: String = "waiting"
)

object MultiplayerManager {

    private val database: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance()
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

    fun createRoom(boardSize: Int, callback: (String) -> Unit) {
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
                "status" to "waiting"
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

                    val updates = mapOf(
                        "board" to cells.joinToString(","),
                        "currentPlayer" to if (hasWon || isDraw) currentPlayer else nextPlayer,
                        "winner" to if (hasWon) currentPlayer else "",
                        "isDraw" to isDraw,
                        "status" to if (hasWon || isDraw) "finished" else "playing"
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
                        status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
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
