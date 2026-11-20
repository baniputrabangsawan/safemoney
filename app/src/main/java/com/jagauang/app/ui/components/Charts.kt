package com.jagauang.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Sequential blue-indigo ramp: adjacent categories differ by lightness, not hue.
 * One hue family keeps the donut calm and readable instead of a rainbow of accents.
 */
val chartColors = listOf(
    Color(0xFF23364D),
    Color(0xFF2E475F),
    Color(0xFF3B5A75),
    Color(0xFF4C6E8C),
    Color(0xFF6184A2),
    Color(0xFF7B9BB6),
    Color(0xFF99B3C9),
    Color(0xFFBACDDD),
    Color(0xFFDCE6EF),
)

@Composable
fun DonutChart(slices: List<Pair<Color, Long>>, modifier: Modifier = Modifier) {
    val total = remember(slices) { slices.sumOf { it.second } }
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Canvas(modifier) {
        val diameter = size.minDimension
        val stroke = diameter * 0.115f
        // Arcs are stroked around their path, so reserve half the stroke plus edge clearance.
        val inset = stroke / 2f + 2.dp.toPx()
        val arcDiameter = diameter - inset * 2f
        if (arcDiameter <= 0f) return@Canvas
        val arcSize = Size(arcDiameter, arcDiameter)
        val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f)
        val style = Stroke(width = stroke)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = style,
        )
        if (total > 0) {
            var start = -90f
            slices.forEach { (color, value) ->
                val sweep = (value.toDouble() / total * 360.0).toFloat()
                val gap = if (slices.size > 1) minOf(3f, sweep * 0.2f) else 0f
                drawArc(
                    color = color,
                    startAngle = start + gap / 2f,
                    sweepAngle = sweep - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style,
                )
                start += sweep
            }
        }
    }
}
