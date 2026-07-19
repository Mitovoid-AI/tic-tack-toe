package com.tictactoe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import com.tictactoe.util.MarkerManager

@Composable
fun CellView(
    value: String,
    isWinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = if (value.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "cell_anim"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isWinning) 0.8f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "glow_anim"
    )

    val primaryColor = AppConfig.primaryColor()
    val secondaryColor = AppConfig.secondaryColor()
    val surfaceColor = AppConfig.surfaceColor()

    // Determine marker text
    val markerText = when (value) {
        "X" -> MarkerManager.markerX
        "O" -> MarkerManager.markerO
        else -> ""
    }

    // Check if marker is a single emoji (multi-char Unicode) or plain text
    val isEmoji = markerText.length > 1 || markerText.codePointAt(0) > 127

    val markerColor = when (value) {
        "X" -> if (isWinning) AppConfig.accentColor() else primaryColor
        "O" -> if (isWinning) AppConfig.accentColor() else secondaryColor
        else -> primaryColor
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .clickable(enabled = value.isEmpty()) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Glow for winning cells
            if (glowAlpha > 0f) {
                drawCircle(
                    color = markerColor,
                    radius = size.minDimension / 2,
                    alpha = glowAlpha * 0.15f
                )
            }

            // Cell background
            drawRoundRect(
                color = surfaceColor.copy(alpha = 0.4f),
                cornerRadius = CornerRadius(12.dp.toPx()),
                size = size
            )

            // If it's a plain X or O, draw with lines (original style)
            if (!isEmoji && value == "X" && animProgress > 0f) {
                val padding = size.minDimension * 0.2f
                val strokeWidth = 6.dp.toPx()

                drawLine(
                    color = markerColor,
                    start = Offset(padding, padding),
                    end = Offset(
                        padding + (size.width - 2 * padding) * animProgress,
                        padding + (size.height - 2 * padding) * animProgress
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                if (animProgress > 0.5f) {
                    val p2 = (animProgress - 0.5f) * 2f
                    drawLine(
                        color = markerColor,
                        start = Offset(size.width - padding, padding),
                        end = Offset(
                            size.width - padding - (size.width - 2 * padding) * p2,
                            padding + (size.height - 2 * padding) * p2
                        ),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            if (!isEmoji && value == "O" && animProgress > 0f) {
                val padding = size.minDimension * 0.2f
                drawArc(
                    color = markerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animProgress,
                    useCenter = false,
                    topLeft = Offset(padding, padding),
                    size = Size(size.width - 2 * padding, size.height - 2 * padding),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // For emoji or custom text markers, show as text
        if (value.isNotEmpty() && (isEmoji || animProgress >= 1f)) {
            Text(
                text = markerText,
                color = markerColor,
                fontSize = if (isEmoji) 32.sp else 36.sp,
                fontWeight = if (isEmoji) FontWeight.Normal else FontWeight.Bold,
                modifier = Modifier.alpha(animProgress)
            )
        }
    }
}
