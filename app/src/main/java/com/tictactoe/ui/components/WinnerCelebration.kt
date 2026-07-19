package com.tictactoe.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Balloon(
    val x: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val delay: Long,
    val wobble: Float
)

data class Confetti(
    val x: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val delay: Long,
    val rotation: Float
)

@Composable
fun WinnerCelebration(
    winner: String,
    isDraw: Boolean,
    onDismiss: () -> Unit
) {
    val primary = AppConfig.primaryColor()
    val secondary = AppConfig.secondaryColor()
    val accent = AppConfig.accentColor()
    val textPrimary = AppConfig.textPrimaryColor()

    val dialogAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0f) }

    // Generate random balloons
    val balloons = remember {
        List(15) {
            Balloon(
                x = Random.nextFloat(),
                color = listOf(primary, secondary, accent, Color(0xFFFFD700), Color(0xFF00FF88)).random(),
                size = Random.nextFloat() * 30f + 20f,
                speed = Random.nextFloat() * 0.8f + 0.4f,
                delay = Random.nextLong(0, 500),
                wobble = Random.nextFloat() * 40f - 20f
            )
        }
    }

    // Generate confetti particles
    val confetti = remember {
        List(30) {
            Confetti(
                x = Random.nextFloat(),
                color = listOf(primary, secondary, accent, Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF00FF88)).random(),
                size = Random.nextFloat() * 8f + 4f,
                speed = Random.nextFloat() * 1f + 0.5f,
                delay = Random.nextLong(0, 800),
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val balloonProgress = remember { Animatable(0f) }
    val confettiProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { dialogAlpha.animateTo(1f, tween(300)) }
        launch { textScale.animateTo(1f, tween(500)) }
        launch { balloonProgress.animateTo(1f, tween(3000, easing = LinearEasing)) }
        launch {
            delay(200)
            confettiProgress.animateTo(1f, tween(2500, easing = LinearEasing))
        }
        delay(3500)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha.value)
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // Animated balloons rising from bottom
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Draw balloons
            balloons.forEach { balloon ->
                val progress = ((balloonProgress.value * 1.2f) - (balloon.delay / 3000f)).coerceIn(0f, 1f)
                if (progress > 0f) {
                    val y = canvasH * (1f - progress * balloon.speed)
                    val x = canvasW * balloon.x + sin(progress * 10f) * balloon.wobble

                    // Balloon body
                    drawOval(
                        color = balloon.color.copy(alpha = 0.8f),
                        topLeft = Offset(x - balloon.size / 2, y - balloon.size),
                        size = androidx.compose.ui.geometry.Size(balloon.size, balloon.size * 1.3f)
                    )
                    // Balloon highlight
                    drawOval(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(x - balloon.size / 4, y - balloon.size * 0.8f),
                        size = androidx.compose.ui.geometry.Size(balloon.size / 3, balloon.size / 2)
                    )
                    // String
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(x, y + balloon.size * 0.3f),
                        end = Offset(x + sin(progress * 8f) * 10f, y + balloon.size * 0.3f + 40f),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Draw confetti
            confetti.forEach { c ->
                val progress = ((confettiProgress.value * 1.3f) - (c.delay / 2500f)).coerceIn(0f, 1f)
                if (progress > 0f) {
                    val y = canvasH * (1f - progress * c.speed)
                    val x = canvasW * c.x + sin(progress * 15f) * 30f

                    drawCircle(
                        color = c.color.copy(alpha = (1f - progress) * 0.9f),
                        radius = c.size,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Winner card in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (300 * (1f - textScale.value)).dp)
                .alpha(textScale.value)
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppConfig.surfaceColor().copy(alpha = 0.95f),
                            AppConfig.surfaceColor().copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(32.dp)
        ) {
            if (isDraw) {
                Text(
                    text = "DRAW!",
                    color = accent,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nobody wins this time",
                    color = textPrimary.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            } else {
                val winColor = if (winner == "X") primary else secondary
                Text(
                    text = "PLAYER $winner",
                    color = winColor.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "WINS!",
                    color = winColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Decorative dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(winColor.copy(alpha = 0.4f + i * 0.12f))
                        )
                    }
                }
            }
        }
    }
}
