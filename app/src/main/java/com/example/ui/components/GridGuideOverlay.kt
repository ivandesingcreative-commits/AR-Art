package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class GridType(val title: String) {
    RULE_OF_THIRDS("Regla de Tercios"),
    GOLDEN_RATIO("Proporción Áurea"),
    GRID_10X10("Malla 10x10"),
    DIAGONAL("Ejes Diagonales"),
    CONCENTRIC_CIRCLES("Círculos Concéntricos"),
    NONE("Sin Guía")
}

@Composable
fun GridGuideOverlay(
    gridType: GridType,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0x7700E5FF)
) {
    if (gridType == GridType.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val strokePx = 1.dp.toPx()

        when (gridType) {
            GridType.RULE_OF_THIRDS -> {
                val x1 = w / 3f
                val x2 = 2f * w / 3f
                val y1 = h / 3f
                val y2 = 2f * h / 3f

                drawLine(lineColor, Offset(x1, 0f), Offset(x1, h), strokePx)
                drawLine(lineColor, Offset(x2, 0f), Offset(x2, h), strokePx)
                drawLine(lineColor, Offset(0f, y1), Offset(w, y1), strokePx)
                drawLine(lineColor, Offset(0f, y2), Offset(w, y2), strokePx)

                // Draw power points
                val radius = 5.dp.toPx()
                drawCircle(lineColor, radius, Offset(x1, y1))
                drawCircle(lineColor, radius, Offset(x2, y1))
                drawCircle(lineColor, radius, Offset(x1, y2))
                drawCircle(lineColor, radius, Offset(x2, y2))
            }

            GridType.GOLDEN_RATIO -> {
                val phi = 1.618f
                val x1 = w / phi
                val y1 = h / phi

                drawLine(lineColor, Offset(w - x1, 0f), Offset(w - x1, h), strokePx)
                drawLine(lineColor, Offset(x1, 0f), Offset(x1, h), strokePx)
                drawLine(lineColor, Offset(0f, h - y1), Offset(w, h - y1), strokePx)
                drawLine(lineColor, Offset(0f, y1), Offset(w, y1), strokePx)
            }

            GridType.GRID_10X10 -> {
                for (i in 1..9) {
                    val x = w * i / 10f
                    val y = h * i / 10f
                    val alpha = if (i == 5) 0.8f else 0.35f
                    val color = lineColor.copy(alpha = alpha)
                    drawLine(color, Offset(x, 0f), Offset(x, h), strokePx)
                    drawLine(color, Offset(0f, y), Offset(w, y), strokePx)
                }
            }

            GridType.DIAGONAL -> {
                drawLine(lineColor, Offset(0f, 0f), Offset(w, h), strokePx)
                drawLine(lineColor, Offset(w, 0f), Offset(0f, h), strokePx)
                drawLine(lineColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokePx)
                drawLine(lineColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokePx)
            }

            GridType.CONCENTRIC_CIRCLES -> {
                val center = Offset(w / 2f, h / 2f)
                val maxR = minOf(w, h) / 2f
                for (i in 1..4) {
                    val r = maxR * i / 4f
                    drawCircle(
                        color = lineColor,
                        radius = r,
                        center = center,
                        style = Stroke(width = strokePx)
                    )
                }
                drawLine(lineColor, Offset(center.x, 0f), Offset(center.x, h), strokePx)
                drawLine(lineColor, Offset(0f, center.y), Offset(w, center.y), strokePx)
            }

            GridType.NONE -> {}
        }
    }
}
