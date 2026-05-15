package org.blindsystems.bop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.blindsystems.bop.core.Segment
import kotlin.math.sin

/**
 * A premium waveform / progress bar with A-B markers and a draggable playhead.
 * Displays a simulated waveform for aesthetic appeal.
 */
@Composable
fun WaveformWidget(
    durationMs: Long,
    positionMs: Long,
    loopAMs: Long?,
    loopBMs: Long?,
    segments: List<Segment> = emptyList(),
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val primaryColor = colors.primary
    val surfaceVariant = colors.surfaceVariant
    val loopAColor = Color(0xFF81C784)
    val loopBColor = Color(0xFFFFB74D)
    val segmentColor = Color(0xFFCE93D8)

    // Generate simulated waveform data once
    val waveformData = remember(durationMs) {
        if (durationMs <= 0) FloatArray(0)
        else {
            FloatArray(100) { i ->
                val x = i.toDouble() / 100.0
                val amp = (0.2 + 0.5 * kotlin.math.abs(
                    kotlin.math.sin(x * 20.0) * kotlin.math.sin(x * 7.0) + 0.3 * kotlin.math.sin(x * 50.0)
                )).toFloat()
                amp.coerceIn(0.1f, 1.0f)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp),
        color = colors.surface.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.3f)),
        tonalElevation = 4.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0) {
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((fraction * durationMs).toLong())
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures { change, _ ->
                        if (durationMs > 0) {
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            onSeek((fraction * durationMs).toLong())
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val barSpacing = 4f
            val barWidth = (w / 100f) - barSpacing

            if (durationMs > 0) {
                val progressX = (positionMs.toFloat() / durationMs) * w

                // ── Simulated Waveform Bars ──────────────────────────────────
                waveformData.forEachIndexed { i, amplitude ->
                    val x = i * (barWidth + barSpacing) + barSpacing / 2
                    val barHeight = amplitude * h * 0.8f
                    val top = midY - barHeight / 2
                    val isPast = x < progressX

                    drawRoundRect(
                        color = if (isPast) primaryColor else colors.onSurfaceVariant.copy(alpha = 0.3f),
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2)
                    )
                }

                // ── A-B Region Highlight ─────────────────────────────────────
                if (loopAMs != null && loopBMs != null && loopAMs < loopBMs) {
                    val ax = (loopAMs.toFloat() / durationMs) * w
                    val bx = (loopBMs.toFloat() / durationMs) * w
                    drawRect(
                        color = loopAColor.copy(alpha = 0.15f),
                        topLeft = Offset(ax, 0f),
                        size = Size(bx - ax, h)
                    )
                    // Draw selection border
                    drawLine(loopAColor.copy(0.5f), Offset(ax, 0f), Offset(bx, 0f), 2f)
                    drawLine(loopAColor.copy(0.5f), Offset(ax, h), Offset(bx, h), 2f)
                }

                // ── Markers ──────────────────────────────────────────────────
                segments.forEach { seg ->
                    val sx = (seg.start.toFloat() / durationMs) * w
                    drawLine(
                        color = segmentColor.copy(alpha = 0.8f),
                        start = Offset(sx, 4f),
                        end = Offset(sx, h - 4f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                loopAMs?.let {
                    val ax = (it.toFloat() / durationMs) * w
                    drawLine(loopAColor, Offset(ax, 0f), Offset(ax, h), strokeWidth = 6f)
                    drawCircle(loopAColor, 8f, Offset(ax, 0f))
                }

                loopBMs?.let {
                    val bx = (it.toFloat() / durationMs) * w
                    drawLine(loopBColor, Offset(bx, 0f), Offset(bx, h), strokeWidth = 6f)
                    drawCircle(loopBColor, 8f, Offset(bx, h))
                }

                // ── Playhead ─────────────────────────────────────────────────
                drawLine(
                    color = Color.White,
                    start = Offset(progressX, 0f),
                    end = Offset(progressX, h),
                    strokeWidth = 4f
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, primaryColor)),
                    radius = 12f,
                    center = Offset(progressX, midY)
                )
            }
        }
    }
}
