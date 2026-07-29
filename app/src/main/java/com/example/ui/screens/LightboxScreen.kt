package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ReferencePhotoEntity
import com.example.ui.components.CameraPreview
import com.example.ui.components.GridGuideOverlay
import com.example.ui.components.GridType
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val referencePhotos by viewModel.referencePhotos.collectAsState()
    val selectedPhoto by viewModel.selectedReferencePhoto.collectAsState()

    var opacity by remember { mutableFloatStateOf(0.55f) }
    var isTraceMode by remember { mutableStateOf(false) }
    var traceThreshold by remember { mutableFloatStateOf(0.5f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var gridType by remember { mutableStateOf(GridType.RULE_OF_THIRDS) }

    var lensFacing by remember { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var showControls by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addReferencePhoto(it, "FRONTAL")
            Toast.makeText(context, "Foto de referencia agregada", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mesa de Luz (Lightbox)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            currentProject?.title ?: "Superposición de Referencia",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_lightbox_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK)
                                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                            else androidx.camera.core.CameraSelector.LENS_FACING_BACK
                        }
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Cambiar Cámara", tint = ArNeonGold)
                    }
                    IconButton(
                        onClick = { showControls = !showControls }
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Ajustes", tint = Color.White)
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
            // Background Live Camera Stream
            CameraPreview(
                lensFacing = lensFacing,
                modifier = Modifier.fillMaxSize()
            )

            // Reference Photo Overlay View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotate ->
                            scale = (scale * zoom).coerceIn(0.2f, 5.0f)
                            rotation += rotate
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val currentPhotoUri = selectedPhoto?.imageUri ?: referencePhotos.firstOrNull()?.imageUri

                if (currentPhotoUri != null) {
                    val colorMatrix = if (isTraceMode) {
                        // High contrast trace filter matrix
                        ColorMatrix(
                            floatArrayOf(
                                3.0f, 0.0f, 0.0f, 0.0f, -255f * traceThreshold,
                                0.0f, 3.0f, 0.0f, 0.0f, -255f * traceThreshold,
                                0.0f, 0.0f, 3.0f, 0.0f, -255f * traceThreshold,
                                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                            )
                        )
                    } else null

                    AsyncImage(
                        model = currentPhotoUri,
                        contentDescription = "Foto de referencia",
                        colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(opacity)
                            .graphicsLayer {
                                scaleX = if (flipHorizontal) -scale else scale
                                scaleY = scale
                                rotationZ = rotation
                            }
                    )
                } else {
                    // Sample Default Lightbox Reference Overlay when no custom image added yet
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .alpha(opacity)
                            .border(2.dp, ArNeonCyan, RoundedCornerShape(16.dp))
                            .background(StudioDarkSurface.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = ArNeonCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mesa de Luz Lista",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sube o selecciona una foto de referencia abajo",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Grid Guide Overlay Lines
            GridGuideOverlay(
                gridType = gridType,
                lineColor = ArNeonCyan
            )

            // Control Panels Overlay
            AnimatedVisibility(
                visible = showControls,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioDarkSurface.copy(alpha = 0.9f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reference Image Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Fotos de Referencia",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.testTag("btn_upload_reference_photo")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Subir Foto", tint = TerracottaPrimary)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(referencePhotos) { photo ->
                            val isSel = selectedPhoto?.id == photo.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) TerracottaPrimary else StudioDarkCard,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { viewModel.selectReferencePhoto(photo) }
                            ) {
                                AsyncImage(
                                    model = photo.imageUri,
                                    contentDescription = photo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Sliders & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Opacidad ${(opacity * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.width(90.dp)
                        )
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Grid Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridOn, contentDescription = null, tint = ArNeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guía Grid", fontSize = 12.sp, color = Color.White)
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(GridType.entries.toTypedArray()) { g ->
                                val isSel = g == gridType
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) ArNeonCyan else StudioDarkCard,
                                    modifier = Modifier.clickable { gridType = g }
                                ) {
                                    Text(
                                        g.title.take(8),
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.Black else Color.LightGray,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Contour Trace Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Modo Calco (Trazo Contorno)", fontSize = 13.sp, color = Color.White)
                            Text("Filtro de alto contraste para calcar formas", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isTraceMode,
                            onCheckedChange = { isTraceMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                        )
                    }

                    if (isTraceMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Umbral de trazo", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.width(90.dp))
                            Slider(
                                value = traceThreshold,
                                onValueChange = { traceThreshold = it },
                                valueRange = 0.1f..0.9f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Transform Actions Row (Flip, Reset, Capture)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { flipHorizontal = !flipHorizontal }) {
                            Icon(Icons.Default.Flip, contentDescription = "Voltear", tint = Color.White)
                        }
                        Button(
                            onClick = {
                                scale = 1f
                                rotation = 0f
                                flipHorizontal = false
                                opacity = 0.5f
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard)
                        ) {
                            Text("Restablecer", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "📷 Captura de mesa de luz guardada en el proyecto", Toast.LENGTH_SHORT).show()
                                viewModel.addTimelapseSnapshot("sample_lightbox_capture_${System.currentTimeMillis()}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.testTag("btn_capture_lightbox_snapshot")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capturar Avance")
                        }
                    }
                }
            }
        }
    }
}
