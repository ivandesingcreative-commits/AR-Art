package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ReferencePhotoEntity
import com.example.model.Guide3DType
import com.example.model.Mesh3D
import com.example.ui.components.CameraPreview
import com.example.ui.components.GridGuideOverlay
import com.example.ui.components.GridType
import com.example.ui.components.Sculpt3DViewport
import com.example.ui.components.takeCameraPhoto
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import com.example.util.rememberOrientationDegrees

data class SpatialAnglePreset(
    val label: String,
    val degrees: Float,
    val description: String
)

val CANONICAL_ANGLES = listOf(
    SpatialAnglePreset("Frente (0°)", 0f, "Vista frontal directa"),
    SpatialAnglePreset("3/4 Frontal (45°)", 45f, "Diagonal tres cuartos"),
    SpatialAnglePreset("Perfil Izq (90°)", 90f, "Lado izquierdo 90°"),
    SpatialAnglePreset("3/4 Trasero (135°)", 135f, "Diagonal trasera"),
    SpatialAnglePreset("Atrás (180°)", 180f, "Espalda / Reverso 180°"),
    SpatialAnglePreset("Perfil Der (270°)", 270f, "Lado derecho 270°")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onNavigateToPrintableMarkers: () -> Unit = {}
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

    // Spatial & Gyroscope Alignment Angle (0° to 360°)
    var enableGyroTracking by remember { mutableStateOf(false) }
    var manualAngleDegrees by remember { mutableFloatStateOf(0f) }
    var showSpatialMarkers by remember { mutableStateOf(true) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    val sensorAzimuth = rememberOrientationDegrees(enabled = enableGyroTracking)

    val activeAngle = if (enableGyroTracking) sensorAzimuth else manualAngleDegrees

    // Geometric 3D Guide overlay mode
    var enable3DGuideOverlay by remember { mutableStateOf(false) }
    var guide3DType by remember { mutableStateOf(Guide3DType.SPHERE) }

    var lensFacing by remember { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var showControls by remember { mutableStateOf(true) }
    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var showNameDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var customViewName by remember { mutableStateOf("Frente") }

    LaunchedEffect(Unit) {
        viewModel.ensureDefaultProject {}
    }

    // Auto-Select Photo matching active angle if gyro tracking or dial is used
    LaunchedEffect(activeAngle, referencePhotos.size) {
        if (referencePhotos.isNotEmpty()) {
            val closest = referencePhotos.minByOrNull { photo ->
                val photoAngle = when (photo.title.lowercase()) {
                    "frente", "frontal" -> 0f
                    "3/4 frontal", "3/4 izq" -> 45f
                    "lado izquierdo", "perfil izq", "perfil" -> 90f
                    "3/4 trasero" -> 135f
                    "atrás", "espalda", "reverso" -> 180f
                    "lado derecho", "perfil der" -> 270f
                    else -> 0f
                }
                val diff = Math.abs(photoAngle - (activeAngle % 360f))
                if (diff > 180f) 360f - diff else diff
            }
            closest?.let { viewModel.selectReferencePhoto(it) }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingUri = it
            showNameDialog = true
        }
    }

    val currentMesh = remember(guide3DType) {
        when (guide3DType) {
            Guide3DType.SPHERE -> Mesh3D.createSphere()
            Guide3DType.CUBE -> Mesh3D.createCube()
            Guide3DType.CYLINDER -> Mesh3D.createCylinder()
            Guide3DType.CONE -> Mesh3D.createCone()
            Guide3DType.HEAD_BUST -> Mesh3D.createHeadBust()
            Guide3DType.TORSO -> Mesh3D.createCylinder(radius = 90f, height = 240f)
            Guide3DType.POT_VASE -> Mesh3D.createSphere(radius = 110f, rings = 10, segments = 16)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mesa de Luz (Estudio AR)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${currentProject?.title ?: "Escultura"} • ${activeAngle.toInt()}°",
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
                    IconButton(onClick = onNavigateToPrintableMarkers, modifier = Modifier.testTag("btn_qr_printable")) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Marcadores y QR", tint = ArNeonCyan)
                    }
                    IconButton(onClick = { showInstructionsDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Guía de Trabajo", tint = ArNeonGold)
                    }
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK)
                                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                            else androidx.camera.core.CameraSelector.LENS_FACING_BACK
                        }
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Cambiar Cámara", tint = Color.White)
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
                onImageCaptureCreated = { activeImageCapture = it },
                modifier = Modifier.fillMaxSize()
            )

            // Geometric 3D Mesh Guide Overlay if enabled
            if (enable3DGuideOverlay) {
                Sculpt3DViewport(
                    mesh = currentMesh,
                    lineColor = ArNeonCyan,
                    accentColor = ArNeonGold,
                    showProportionGuide = true,
                    isWireframe = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Reference Photo Overlay View with Gestures
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
                        contentDescription = selectedPhoto?.title ?: "Foto de referencia",
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
                } else if (!enable3DGuideOverlay) {
                    // Empty state helper when no reference added yet
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .alpha(opacity)
                            .border(2.dp, ArNeonCyan, RoundedCornerShape(16.dp))
                            .background(StudioDarkSurface.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
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
                                "Toca el botón + abajo para agregar referencias (Frente, Perfil, Atrás)",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Spatial Reference Markers Overlay (Circles for X, Triangles for Z, Center Reticle)
            if (showSpatialMarkers) {
                SpatialMarkersOverlay(activeAngle = activeAngle)
            }

            // Grid Guide Overlay Lines
            GridGuideOverlay(
                gridType = gridType,
                lineColor = ArNeonCyan
            )

            // Control Panels & Quick Reference Selector Overlay
            AnimatedVisibility(
                visible = showControls,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioDarkSurface.copy(alpha = 0.94f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // DIAL DE ÁNGULO Y SEGUIMIENTO GIROSCÓPICO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = ArNeonGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ángulo Espacial: ${activeAngle.toInt()}°",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto Giroscopio", fontSize = 11.sp, color = ArNeonCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = enableGyroTracking,
                                onCheckedChange = { enableGyroTracking = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                            )
                        }
                    }

                    // Angle Presets (Frente 0°, 3/4 45°, Lado 90°, Atrás 180°, etc.)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CANONICAL_ANGLES) { preset ->
                            val isSel = Math.abs(preset.degrees - (activeAngle % 360f)) < 25f
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) TerracottaPrimary else StudioDarkCard,
                                modifier = Modifier.clickable {
                                    enableGyroTracking = false
                                    manualAngleDegrees = preset.degrees
                                }
                            ) {
                                Text(
                                    preset.label,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Manual Angle Slider Dial
                    if (!enableGyroTracking) {
                        Slider(
                            value = manualAngleDegrees,
                            onValueChange = { manualAngleDegrees = it },
                            valueRange = 0f..360f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // QUICK ACCESS BAR FOR REFERENCE IMAGES (Frente, Lado, Atrás, etc.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Vistas de Referencia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArNeonGold
                        )
                        TextButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.testTag("btn_upload_reference_photo")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Cargar Vista", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
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
                                    .clickable { viewModel.selectReferencePhoto(photo) }
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) ArNeonGold else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        AsyncImage(
                                            model = photo.imageUri,
                                            contentDescription = photo.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            photo.title.ifBlank { "Vista" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            photo.angle,
                                            fontSize = 10.sp,
                                            color = if (isSel) Color.White.copy(alpha = 0.8f) else Color.LightGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }
                    }

                    // Transparency Slider (Opacidad)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Opacidad: ${(opacity * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3D Pure Geometric Primitives Selector Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, tint = ArNeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Malla 3D Geométrica Primitiva", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(
                            checked = enable3DGuideOverlay,
                            onCheckedChange = { enable3DGuideOverlay = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                        )
                    }

                    if (enable3DGuideOverlay) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Guide3DType.entries.toTypedArray()) { guide ->
                                val isSel = guide == guide3DType
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) ArNeonCyan else StudioDarkCard,
                                    modifier = Modifier.clickable { guide3DType = guide }
                                ) {
                                    Text(
                                        guide.displayName,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.Black else Color.LightGray,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Transform Actions & REAL CAPTURE BUTTON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { flipHorizontal = !flipHorizontal },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Flip, contentDescription = "Voltear", tint = Color.White)
                            }
                            Button(
                                onClick = {
                                    scale = 1f
                                    rotation = 0f
                                    flipHorizontal = false
                                    opacity = 0.55f
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Restablecer", fontSize = 11.sp)
                            }
                        }

                        // REAL PHOTO CAPTURE BUTTON
                        Button(
                            onClick = {
                                takeCameraPhoto(
                                    context = context,
                                    imageCapture = activeImageCapture,
                                    onPhotoCaptured = { savedUri ->
                                        val activeViewLabel = selectedPhoto?.title ?: "Vista ${activeAngle.toInt()}°"
                                        viewModel.addTimelapseSnapshot(savedUri.toString(), stageLabel = activeViewLabel)
                                        Toast.makeText(context, "📸 Captura guardada en Galería de Avances", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("btn_capture_lightbox_snapshot")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capturar Avance", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // SPATIAL WORKSPACE INSTRUCTIONS DIALOG FOR ARTISTS
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            containerColor = StudioDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ArNeonGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guía de Trabajo Referenciado (AR)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Para lograr la máxima precisión en modelado de arcilla o dibujo sin que la imagen se desplace al mover el móvil o la masa:",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioDarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Marcadores de Posición en la Mesa:", fontWeight = FontWeight.Bold, color = ArNeonCyan, fontSize = 12.sp)
                            Text(
                                "Dibuja o coloca 2 marcas simples en tu base o plato giratorio:\n• Círculo (⚪) para el Eje X (Frente 0°).\n• Triángulo (🔺) para el Eje Z (Perfil 90°).",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioDarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("2. Cargar 6 Caras / Ángulos Principales:", fontWeight = FontWeight.Bold, color = ArNeonGold, fontSize = 12.sp)
                            Text(
                                "Toma o sube fotos etiquetando cada vista: Frente (0°), 3/4 Frontal (45°), Lado Izq (90°), 3/4 Trasero (135°), Atrás (180°), Lado Der (270°).",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioDarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("3. Auto-Conmutación por Giroscopio o Rueda:", fontWeight = FontWeight.Bold, color = TerracottaPrimary, fontSize = 12.sp)
                            Text(
                                "Activa 'Auto Giroscopio' para que al mover el móvil alrededor de la escultura la referencia cambie automáticamente al ángulo correspondiente. Si el móvil está fijo, usa la Rueda de Ángulos (0°-360°).",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInstructionsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Reference View Naming Dialog (Frente, Lado, Atrás, etc.)
    if (showNameDialog && pendingUri != null) {
        val presetViews = listOf("Frente", "3/4 Frontal", "Lado Izquierdo", "3/4 Trasero", "Atrás", "Lado Derecho", "Vista Superior")

        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
                pendingUri = null
            },
            containerColor = StudioDarkSurface,
            title = {
                Text("Etiquetar Vista de Referencia Espacial", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Selecciona la posición angular de esta foto para anclarla espacialmente:",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetViews) { viewLabel ->
                            val isSel = viewLabel == customViewName
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) TerracottaPrimary else StudioDarkCard,
                                modifier = Modifier.clickable { customViewName = viewLabel }
                            ) {
                                Text(
                                    viewLabel,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customViewName,
                        onValueChange = { customViewName = it },
                        label = { Text("Nombre / Ángulo Personalizado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUri?.let { uri ->
                            viewModel.addReferencePhoto(uri, viewName = customViewName, angle = customViewName)
                            Toast.makeText(context, "Vista '$customViewName' agregada", Toast.LENGTH_SHORT).show()
                        }
                        showNameDialog = false
                        pendingUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Guardar Referencia")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNameDialog = false
                    pendingUri = null
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SpatialMarkersOverlay(activeAngle: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Center alignment crosshair
        drawLine(
            color = Color.Cyan.copy(alpha = 0.35f),
            start = Offset(centerX - 30f, centerY),
            end = Offset(centerX + 30f, centerY),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Cyan.copy(alpha = 0.35f),
            start = Offset(centerX, centerY - 30f),
            end = Offset(centerX, centerY + 30f),
            strokeWidth = 2f
        )

        // Marker 1: Circle (X axis / Frente)
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.45f),
            radius = 16f,
            center = Offset(centerX - 160f, centerY + 180f),
            style = Stroke(width = 3f)
        )

        // Marker 2: Triangle (Z axis / Perfil)
        val path = Path().apply {
            moveTo(centerX + 160f, centerY + 165f)
            lineTo(centerX + 145f, centerY + 195f)
            lineTo(centerX + 175f, centerY + 195f)
            close()
        }
        drawPath(
            path = path,
            color = Color.Cyan.copy(alpha = 0.45f),
            style = Stroke(width = 3f)
        )
    }
}
