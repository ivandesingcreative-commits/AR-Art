package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CameraPreview
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlowMoInspectScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var zoomLevel by remember { mutableFloatStateOf(2f) }
    var isSlowMoPlaying by remember { mutableStateOf(true) }
    var slowMoSpeed by remember { mutableFloatStateOf(0.25f) } // 0.125x, 0.25x, 0.5x
    var isCrackEnhancerMode by remember { mutableStateOf(false) }
    var currentSimulatedFrame by remember { mutableIntStateOf(12) }

    LaunchedEffect(isSlowMoPlaying, slowMoSpeed) {
        while (isSlowMoPlaying) {
            delay((100 / slowMoSpeed).toLong())
            currentSimulatedFrame = (currentSimulatedFrame + 1) % 120
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Inspección en Cámara Lenta",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Análisis minucioso de grietas, uniones y acabado",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_slowmo_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkSurface.copy(alpha = 0.85f)
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Camera or Zoomed Preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(zoomLevel),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(modifier = Modifier.fillMaxSize())
            }

            // Loupe Reticle Overlay & High Contrast Crack Inspector Crosshair
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = 130.dp.toPx()

                // Loupe circle boundary
                drawCircle(
                    color = if (isCrackEnhancerMode) Color.Red else ArNeonCyan,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Crosshair lines inside loupe
                drawLine(
                    color = ArNeonCyan.copy(alpha = 0.6f),
                    start = Offset(cx - radius, cy),
                    end = Offset(cx + radius, cy),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = ArNeonCyan.copy(alpha = 0.6f),
                    start = Offset(cx, cy - radius),
                    end = Offset(cx, cy + radius),
                    strokeWidth = 1.5.dp.toPx()
                )

                if (isCrackEnhancerMode) {
                    // Draw simulated crack detection highlights
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.35f),
                        radius = 40.dp.toPx(),
                        center = Offset(cx + 20f, cy - 30f)
                    )
                }
            }

            // High FPS Frame Counter HUD Top Right
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioDarkSurface.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        "120 FPS HIGH-SPEED",
                        color = ArNeonGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        "Frame: $currentSimulatedFrame / 120",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        "Lupa: ${zoomLevel.toInt()}x",
                        color = ArNeonCyan,
                        fontSize = 11.sp
                    )
                }
            }

            // Slow Motion Controls Panel Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(StudioDarkSurface.copy(alpha = 0.9f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Zoom Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = ArNeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aumento Lupa Digital", fontSize = 12.sp, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1f, 2f, 4f, 8f).forEach { z ->
                            val isSel = zoomLevel == z
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ArNeonCyan else StudioDarkCard,
                                modifier = Modifier.clickable { zoomLevel = z }
                            ) {
                                Text(
                                    "${z.toInt()}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Crack & Texture Enhancer Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Filter, contentDescription = null, tint = TerracottaPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detector de Grietas y Poros en Arcilla", fontSize = 12.sp, color = Color.White)
                    }
                    Switch(
                        checked = isCrackEnhancerMode,
                        onCheckedChange = { isCrackEnhancerMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                    )
                }

                // Slow Motion Video Controls (Play/Pause, Speeds)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentSimulatedFrame = (currentSimulatedFrame - 1).coerceAtLeast(0) }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White)
                    }

                    IconButton(
                        onClick = { isSlowMoPlaying = !isSlowMoPlaying },
                        modifier = Modifier.testTag("btn_slowmo_play")
                    ) {
                        Icon(
                            imageVector = if (isSlowMoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pausa",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = { currentSimulatedFrame = (currentSimulatedFrame + 1) % 120 }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White)
                    }

                    // Speed pills
                    listOf(0.125f, 0.25f, 0.5f).forEach { speed ->
                        val isSel = slowMoSpeed == speed
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) TerracottaPrimary else StudioDarkCard,
                            modifier = Modifier.clickable { slowMoSpeed = speed }
                        ) {
                            Text(
                                "${speed}x",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "🔍 Captura de detalle en alta resolución guardada", Toast.LENGTH_SHORT).show()
                        viewModel.addTimelapseSnapshot("sample_slowmo_detail_${System.currentTimeMillis()}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capturar Detalle de Grieta / Uniones")
                }
            }
        }
    }
}
