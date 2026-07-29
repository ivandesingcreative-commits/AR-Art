package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.Mesh3D
import com.example.model.Vector3D
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Sculpt3DViewport(
    mesh: Mesh3D,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFFFB74D),
    showProportionGuide: Boolean = true,
    isWireframe: Boolean = true,
    onTransformChanged: (rotX: Float, rotY: Float, scale: Float) -> Unit = { _, _, _ -> }
) {
    var rotX by remember { mutableFloatStateOf(15f) }
    var rotY by remember { mutableFloatStateOf(30f) }
    var scale by remember { mutableFloatStateOf(1.2f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    rotY += pan.x * 0.4f
                    rotX -= pan.y * 0.4f
                    scale = (scale * zoom).coerceIn(0.3f, 4.0f)
                    onTransformChanged(rotX, rotY, scale)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f + offsetX
            val cy = size.height / 2f + offsetY

            val radX = Math.toRadians(rotX.toDouble())
            val radY = Math.toRadians(rotY.toDouble())

            val cosX = cos(radX).toFloat()
            val sinX = sin(radX).toFloat()
            val cosY = cos(radY).toFloat()
            val sinY = sin(radY).toFloat()

            // Project 3D vertices to 2D
            val projectedPoints = mesh.vertices.map { v ->
                // Rotate around Y axis
                val x1 = v.x * cosY + v.z * sinY
                val y1 = v.y
                val z1 = -v.x * sinY + v.z * cosY

                // Rotate around X axis
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX

                // Perspective projection
                val distance = 400f
                val fov = distance / (distance + z2 * 0.3f)

                Offset(
                    x = cx + x2 * scale * fov,
                    y = cy + y2 * scale * fov
                )
            }

            // Draw Edges / Wireframe
            val strokeWidth = if (isWireframe) 2.dp.toPx() else 3.5.dp.toPx()
            for (edge in mesh.edges) {
                if (edge.start in projectedPoints.indices && edge.end in projectedPoints.indices) {
                    val p1 = projectedPoints[edge.start]
                    val p2 = projectedPoints[edge.end]
                    drawLine(
                        color = lineColor.copy(alpha = 0.85f),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw Vertex Nodes
            for (p in projectedPoints) {
                drawCircle(
                    color = accentColor,
                    radius = 3.dp.toPx(),
                    center = p
                )
            }

            // Proportion Guide Overlays (Axis & Eye / Mouth lines for head/figure)
            if (showProportionGuide) {
                // Vertical Center Axis
                drawLine(
                    color = accentColor.copy(alpha = 0.4f),
                    start = Offset(cx, cy - size.height * 0.4f),
                    end = Offset(cx, cy + size.height * 0.4f),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f)
                    )
                )

                // Horizontal Eye / Jaw Line
                drawLine(
                    color = accentColor.copy(alpha = 0.4f),
                    start = Offset(cx - size.width * 0.35f, cy),
                    end = Offset(cx + size.width * 0.35f, cy),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f)
                    )
                )

                // Golden Ratio Proportion Box
                drawRect(
                    color = lineColor.copy(alpha = 0.25f),
                    topLeft = Offset(cx - 150f * scale, cy - 200f * scale),
                    size = androidx.compose.ui.geometry.Size(300f * scale, 400f * scale),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 8f)
                        )
                    )
                )
            }
        }
    }
}
