package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Guide3DType
import com.example.model.Mesh3D
import com.example.ui.components.CameraPreview
import com.example.ui.components.Sculpt3DViewport
import com.example.ui.components.takeCameraPhoto
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import com.example.util.rememberOrientationDegrees

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.util.Obj3DFileParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArSculptScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onNavigateToPrintableMarkers: () -> Unit = {},
    onNavigateToFileExplorer: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedGuideType by remember { mutableStateOf(Guide3DType.SPHERE) }
    var isCameraArMode by remember { mutableStateOf(true) }
    var isWireframe by remember { mutableStateOf(true) }
    var showProportions by remember { mutableStateOf(true) }
    var enableGyro by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isPanelCollapsed by remember { mutableStateOf(false) }

    var customImportedMesh by remember { mutableStateOf<Mesh3D?>(null) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val azimuthAngle = rememberOrientationDegrees(enabled = enableGyro)

    LaunchedEffect(Unit) {
        viewModel.ensureDefaultProject {}
    }

    val objFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val parsed = Obj3DFileParser.parseObjFile(context, fileUri, "Modelo 3D OBJ")
            if (parsed != null) {
                customImportedMesh = parsed
                Toast.makeText(context, "✅ Modelo 3D .OBJ cargado (${parsed.vertices.size} vértices)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "❌ No se pudo procesar el archivo .OBJ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentMesh = remember(selectedGuideType, customImportedMesh) {
        customImportedMesh ?: when (selectedGuideType) {
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
                            "Superposición AR y Mallas 3D",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Primitivas geométricas • ${azimuthAngle.toInt()}°",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_ar_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFileExplorer, modifier = Modifier.testTag("btn_ar_file_explorer")) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Explorador de Archivos", tint = ArNeonGold)
                    }
                    IconButton(onClick = onNavigateToPrintableMarkers, modifier = Modifier.testTag("btn_ar_qr_printable")) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Marcadores y QR", tint = ArNeonCyan)
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Instrucciones", tint = ArNeonGold)
                    }
                    IconButton(onClick = { isPanelCollapsed = !isPanelCollapsed }) {
                        Icon(if (isPanelCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Plegar/Desplegar", tint = Color.White)
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
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.2f, 5.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            // Camera Background for AR overlay mode
            if (isCameraArMode) {
                CameraPreview(
                    onImageCaptureCreated = { activeImageCapture = it },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(StudioDarkSurface)
                )
            }

            // 3D Mesh Guide Viewport with Transform Offsets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
            ) {
                Sculpt3DViewport(
                    mesh = currentMesh,
                    lineColor = if (isCameraArMode) ArNeonCyan else ArNeonGold,
                    accentColor = ArNeonGold,
                    showProportionGuide = showProportions,
                    isWireframe = isWireframe,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Collapsed Floating Button Bar
            if (isPanelCollapsed) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(StudioDarkSurface.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isPanelCollapsed = false },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard)
                    ) {
                        Icon(Icons.Default.ExpandLess, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mostrar Menú", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            takeCameraPhoto(
                                context = context,
                                imageCapture = activeImageCapture,
                                onPhotoCaptured = { savedUri ->
                                    viewModel.addTimelapseSnapshot(savedUri.toString(), stageLabel = "Comparación 3D")
                                    Toast.makeText(context, "📸 Captura guardada", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capturar", fontSize = 12.sp)
                    }
                }
            } else {
                // AR Studio Control Panel Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(StudioDarkSurface.copy(alpha = 0.92f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header with collapse button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = ArNeonGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Orientación Giroscopio: ${azimuthAngle.toInt()}°", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = enableGyro,
                                onCheckedChange = { enableGyro = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                            )
                            IconButton(onClick = { isPanelCollapsed = true }) {
                                Icon(Icons.Default.ExpandMore, contentDescription = "Ocultar Menú", tint = Color.LightGray)
                            }
                        }
                    }

                    // Geometric Primitives & OBJ Import Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Malla 3D de Guía:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { objFilePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ModelTraining, contentDescription = null, tint = ArNeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cargar OBJ 3D", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Guide3DType.entries.toTypedArray()) { guide ->
                            val isSel = guide == selectedGuideType && customImportedMesh == null
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) ArNeonCyan else StudioDarkCard,
                                modifier = Modifier.clickable {
                                    customImportedMesh = null
                                    selectedGuideType = guide
                                }
                            ) {
                                Text(
                                    guide.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Toggles Row (Camera AR vs Studio 3D & Reset)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Camera, contentDescription = null, tint = ArNeonCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fondo Cámara AR", fontSize = 12.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Centrar 3D", fontSize = 10.sp)
                            }
                            Switch(
                                checked = isCameraArMode,
                                onCheckedChange = { isCameraArMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                            )
                        }
                    }

                    // Capture Progress Button
                    Button(
                        onClick = {
                            takeCameraPhoto(
                                context = context,
                                imageCapture = activeImageCapture,
                                onPhotoCaptured = { savedUri ->
                                    viewModel.addTimelapseSnapshot(savedUri.toString(), stageLabel = "Primitiva 3D: ${customImportedMesh?.name ?: selectedGuideType.displayName}")
                                    Toast.makeText(context, "📸 Captura de comparación 3D guardada", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_capture_ar_comparison")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capturar Comparación 3D")
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = StudioDarkSurface,
            title = {
                Text("Instrucciones de Anclaje Geométrica 3D", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Usa figuras geométricas simples (Esfera, Cubo, Cilindro, Cono) para estructurar el volumen inicial de tu escultura.", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Coloca marcas de referencia física (ej. 🔴 X, 🔺 Z) en tu mesa de modelado.", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Activa el Giroscopio para que el modelo 3D rote conforme mueves la cámara alrededor de la pieza.", fontSize = 12.sp, color = Color.LightGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
