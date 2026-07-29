package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelapseScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val frames by viewModel.timelapseFrames.collectAsState()

    var isPlayingTimelapse by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableIntStateOf(3) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    var selectedFrameForDetail by remember { mutableStateOf<TimelapseFrameEntity?>(null) }
    var isCompareMode by remember { mutableStateOf(false) }
    var compareFrameA by remember { mutableStateOf<TimelapseFrameEntity?>(null) }
    var compareFrameB by remember { mutableStateOf<TimelapseFrameEntity?>(null) }
    var compareSliderPos by remember { mutableFloatStateOf(0.5f) }

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
                            "Galería de Avances y Capturas",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            currentProject?.title ?: "Evolución de la Escultura",
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
                actions = {
                    IconButton(onClick = { isCompareMode = !isCompareMode }) {
                        Icon(
                            Icons.Default.Compare,
                            contentDescription = "Comparar Avances",
                            tint = if (isCompareMode) ArNeonGold else Color.White
                        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SIDE-BY-SIDE COMPARISON MODE OR PLAYBACK VIEWPORT
            if (isCompareMode && frames.size >= 2) {
                val frameA = compareFrameA ?: frames.first()
                val frameB = compareFrameB ?: frames.last()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Comparador de Avances (Antes vs Después)", color = ArNeonGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { isCompareMode = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(compareSliderPos)
                                        .fillMaxSize()
                                ) {
                                    AsyncImage(
                                        model = frameA.imageUri,
                                        contentDescription = "Antes",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                    ) {
                                        Text("Antes", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f - compareSliderPos)
                                        .fillMaxSize()
                                ) {
                                    AsyncImage(
                                        model = frameB.imageUri,
                                        contentDescription = "Después",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = TerracottaPrimary.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text("Después", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ajustar Comparación:", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.width(110.dp))
                            Slider(
                                value = compareSliderPos,
                                onValueChange = { compareSliderPos = it },
                                valueRange = 0.1f..0.9f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // ANIMATED PLAYBACK VIEWPORT
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { selectedFrameForDetail = activeFrame }
                            )

                            // Playback Toggle Overlay
                            Surface(
                                shape = CircleShape,
                                color = StudioDarkSurface.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .size(54.dp)
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

                            // Frame Info Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(
                                        "Fotograma ${currentFrameIndex + 1} / ${frames.size} • ${activeFrame.stageLabel}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activeFrame.timestamp))
                                    Text(
                                        dateStr,
                                        color = ArNeonCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ArNeonGold,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aún no hay capturas de avances",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Presiona 'Capturar Avance' en la Mesa de Luz o Guía AR para registrar fotos de tu escultura",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // GRID HEADER & ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Fotogramas Registrados (${frames.size})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )

                if (frames.size >= 2) {
                    TextButton(onClick = { isCompareMode = !isCompareMode }) {
                        Text(
                            if (isCompareMode) "Ver Grilla" else "Modo Comparación",
                            color = ArNeonCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // GRID OF CAPTURED PHOTOS
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(frames) { frame ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioDarkCard,
                        modifier = Modifier
                            .height(100.dp)
                            .clickable { selectedFrameForDetail = frame }
                            .border(1.dp, StudioDarkSurface, RoundedCornerShape(12.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = frame.imageUri,
                                contentDescription = frame.stageLabel,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    frame.stageLabel,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FULLSCREEN FRAME INSPECTION & DELETION DIALOG
    if (selectedFrameForDetail != null) {
        val frame = selectedFrameForDetail!!
        val dateStr = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault()).format(Date(frame.timestamp))

        AlertDialog(
            onDismissRequest = { selectedFrameForDetail = null },
            containerColor = StudioDarkSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(frame.stageLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(dateStr, color = ArNeonCyan, fontSize = 11.sp)
                    }
                    IconButton(onClick = { selectedFrameForDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        AsyncImage(
                            model = frame.imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTimelapseFrame(frame)
                            Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                            selectedFrameForDetail = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(frame.imageUri))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Avance de Escultura"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 12.sp)
                    }
                }
            }
        )
    }
}
