package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Opacity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // Geometric 3D Guide overlay mode
    var enable3DGuideOverlay by remember { mutableStateOf(false) }
    var guide3DType by remember { mutableStateOf(Guide3DType.HEAD_BUST) }

    var lensFacing by remember { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var showControls by remember { mutableStateOf(true) }
    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var showNameDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var customViewName by remember { mutableStateOf("Frente") }

    LaunchedEffect(Unit) {
        viewModel.ensureDefaultProject {}
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
            Guide3DType.HEAD_BUST -> Mesh3D.createHeadBust()
            Guide3DType.SPHERE -> Mesh3D.createSphere()
            Guide3DType.CYLINDER -> Mesh3D.createCylinder()
            Guide3DType.ANIMAL_FORM -> Mesh3D.createAnimalBody()
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
                            currentProject?.title ?: "Superposición y Referencias",
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
                                "Toca el botón + abajo para agregar una foto (Frente, Lado, Atrás)",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
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

            // Control Panels & Quick Reference Selector Overlay
            AnimatedVisibility(
                visible = showControls,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioDarkSurface.copy(alpha = 0.92f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // QUICK ACCESS BAR FOR REFERENCE IMAGES (Frente, Lado, Atrás, etc.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Acceso Rápido a Referencias",
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
                            Text("+ Agregar Vista", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
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
                            "Transparencia: ${(opacity * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.width(130.dp)
                        )
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3D Geometric Overlay Toggle Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, tint = ArNeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guía 3D Geométrica Overlay", fontSize = 12.sp, color = Color.White)
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

                    // Contour Trace Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Modo Calco (Filtro de Contornos)", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(
                            checked = isTraceMode,
                            onCheckedChange = { isTraceMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                        )
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
                                        val activeViewLabel = selectedPhoto?.title ?: "Vista libre"
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

    // Reference View Naming Dialog (Frente, Lado, Atrás, Figura vs Plano)
    if (showNameDialog && pendingUri != null) {
        val presetViews = listOf("Frente", "Lado Izquierdo", "Lado Derecho", "Atrás", "Vista Superior", "Detalle")

        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
                pendingUri = null
            },
            containerColor = StudioDarkSurface,
            title = {
                Text("Etiquetar Vista de Referencia", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Selecciona la vista (Frente, Lado, Atrás) o escribe un nombre para el artista:",
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
                        label = { Text("Nombre Personalizado") },
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
