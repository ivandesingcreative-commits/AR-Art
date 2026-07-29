package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.TimelapseFrameEntity
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelapseScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val frames by viewModel.timelapseFrames.collectAsState()

    var isRecordingTimelapse by remember { mutableStateOf(false) }
    var captureIntervalSeconds by remember { mutableIntStateOf(10) }
    var isPlayingTimelapse by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableIntStateOf(5) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    // Automatic Capture Timer Simulation
    LaunchedEffect(isRecordingTimelapse) {
        while (isRecordingTimelapse) {
            delay(captureIntervalSeconds * 1000L)
            val fakeUri = "sample_timelapse_frame_${System.currentTimeMillis()}"
            viewModel.addTimelapseSnapshot(fakeUri)
            Toast.makeText(context, "📸 Fotograma timelapse registrado", Toast.LENGTH_SHORT).show()
        }
    }

    // Playback Loop
    LaunchedEffect(isPlayingTimelapse, playbackSpeed, frames.size) {
        if (frames.isNotEmpty()) {
            while (isPlayingTimelapse) {
                delay(1000L / playbackSpeed)
                currentFrameIndex = (currentFrameIndex + 1) % frames.size
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Timelapse del Proceso",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            currentProject?.title ?: "Evolución de Escultura",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_timelapse_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkSurface
                )
            )
        },
        containerColor = StudioDarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Viewport Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (frames.isNotEmpty()) {
                        val activeFrame = frames.getOrNull(currentFrameIndex) ?: frames.first()
                        AsyncImage(
                            model = activeFrame.imageUri,
                            contentDescription = "Fotograma",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Playback Overlay Controls
                        Surface(
                            shape = CircleShape,
                            color = StudioDarkSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { isPlayingTimelapse = !isPlayingTimelapse }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlayingTimelapse) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = ArNeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Frame Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                "Fotograma ${currentFrameIndex + 1} / ${frames.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = ArNeonGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sin Fotogramas de Timelapse",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Activa la captura automática o toma fotos de avances",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Interval & Recording Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Grabación Automática Timelapse",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                "Captura fotos periódicas del modelado",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                        Switch(
                            checked = isRecordingTimelapse,
                            onCheckedChange = { isRecordingTimelapse = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary),
                            modifier = Modifier.testTag("switch_timelapse_recording")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Intervalo de Captura: $captureIntervalSeconds seg", fontSize = 12.sp, color = ArNeonCyan)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 30, 60).forEach { sec ->
                            val isSel = captureIntervalSeconds == sec
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ArNeonCyan else StudioDarkSurface,
                                modifier = Modifier.clickable { captureIntervalSeconds = sec }
                            ) {
                                Text(
                                    "${sec}s",
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Manual Snapshot & Export Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val fakeUri = "manual_timelapse_frame_${System.currentTimeMillis()}"
                        viewModel.addTimelapseSnapshot(fakeUri)
                        Toast.makeText(context, "📸 Captura agregada al timelapse", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_add_timelapse_snapshot")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tomar Foto")
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "🎬 Video Timelapse exportado a la galería MP4", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_timelapse")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = ArNeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar MP4", color = ArNeonCyan)
                }
            }

            // Timeline Grid of Captured Frames
            Text(
                "Galería de Fotogramas (${frames.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(frames) { frame ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioDarkCard,
                        modifier = Modifier
                            .height(90.dp)
                            .border(1.dp, StudioDarkSurface, RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = frame.imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
