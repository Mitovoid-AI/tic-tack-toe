package com.tictactoe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import com.tictactoe.multiplayer.MultiplayerManager
import com.tictactoe.ui.components.GlassCard
import com.tictactoe.ui.components.NeonButton

@Composable
fun OnlineLobbyScreen(
    onBack: () -> Unit,
    onRoomJoined: (String, Boolean) -> Unit // roomCode, isCreator
) {
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    var roomCodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var createdRoomCode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textSecondary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Online Multiplayer",
                color = primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Create Room
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Room", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Host a game and share the code", color = textSecondary, fontSize = 13.sp)

                    if (createdRoomCode.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = createdRoomCode,
                            color = primary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                        Text("Share this code with your friend", color = textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        NeonButton(text = "START GAME", onClick = {
                            onRoomJoined(createdRoomCode, true)
                        }, color = primary)
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        NeonButton(
                            text = if (isLoading) "CREATING..." else "CREATE ROOM",
                            onClick = {
                                isLoading = true
                                MultiplayerManager.createRoom(3) { code ->
                                    isLoading = false
                                    if (code.isNotEmpty()) {
                                        createdRoomCode = code
                                    } else {
                                        statusMessage = "Failed to create room. Check connection."
                                    }
                                }
                            },
                            color = primary,
                            enabled = !isLoading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Join Room
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join Room", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter a code to join a friend's game", color = textSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase().take(6) },
                        label = { Text("Room Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedLabelColor = primary,
                            unfocusedLabelColor = textSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NeonButton(
                        text = if (isLoading) "JOINING..." else "JOIN",
                        onClick = {
                            if (roomCodeInput.length >= 4) {
                                isLoading = true
                                MultiplayerManager.joinRoom(roomCodeInput) { success ->
                                    isLoading = false
                                    if (success) {
                                        onRoomJoined(roomCodeInput, false)
                                    } else {
                                        statusMessage = "Room not found or full."
                                    }
                                }
                            }
                        },
                        color = secondary,
                        enabled = !isLoading && roomCodeInput.length >= 4
                    )
                }
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(statusMessage, color = secondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = primary)
            }
        }
    }
}
