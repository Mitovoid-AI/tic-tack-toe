package com.tictactoe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.tictactoe.config.AppConfig

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

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .clickable(enabled = value.isEmpty()) { onClick() }
    ) {
        // Background glow for winning cells
        if (glowAlpha > 0f) {
            drawCircle(
                color = if (value == "X") primaryColor else secondaryColor,
                radius = size.minDimension / 2,
                alpha = glowAlpha * 0.15f
            )
        }

        // Cell background
        drawRoundRect(
            color = surfaceColor.copy(alpha = 0.4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            size = size
        )

        val strokeWidth = 6.dp.toPx()
        val padding = size.minDimension * 0.2f

        if (value == "X" && animProgress > 0f) {
            val progress = animProgress
            // Draw X
            val color = if (isWinning) AppConfig.accentColor() else primaryColor

            // First line of X
            drawLine(
                color = color,
                start = Offset(padding, padding),
                end = Offset(
                    padding + (size.width - 2 * padding) * progress,
                    padding + (size.height - 2 * padding) * progress
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Second line of X
            if (progress > 0.5f) {
                val p2 = (progress - 0.5f) * 2f
                drawLine(
                    color = color,
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

        if (value == "O" && animProgress > 0f) {
            val color = if (isWinning) AppConfig.accentColor() else secondaryColor
            // Draw O with sweep animation
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animProgress,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = androidx.compose.ui.geometry.Size(
                    size.width - 2 * padding,
                    size.height - 2 * padding
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
