package com.example.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArSculptScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onNavigateToPrintableMarkers: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedGuideType by remember { mutableStateOf(Guide3DType.SPHERE) }
    var isCameraArMode by remember { mutableStateOf(true) }
    var isWireframe by remember { mutableStateOf(true) }
    var showProportions by remember { mutableStateOf(true) }
    var enableGyro by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val azimuthAngle = rememberOrientationDegrees(enabled = enableGyro)

    LaunchedEffect(Unit) {
        viewModel.ensureDefaultProject {}
    }

    val currentMesh = remember(selectedGuideType) {
        when (selectedGuideType) {
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
                    IconButton(onClick = onNavigateToPrintableMarkers, modifier = Modifier.testTag("btn_ar_qr_printable")) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Marcadores y QR", tint = ArNeonCyan)
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Instrucciones", tint = ArNeonGold)
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

            // 3D Mesh Guide Viewport
            Sculpt3DViewport(
                mesh = currentMesh,
                lineColor = if (isCameraArMode) ArNeonCyan else ArNeonGold,
                accentColor = ArNeonGold,
                showProportionGuide = showProportions,
                isWireframe = isWireframe,
                modifier = Modifier.fillMaxSize()
            )

            // AR Studio Control Panel Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(StudioDarkSurface.copy(alpha = 0.92f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gyroscope tracking row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = ArNeonGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Orientación por Giroscopio: ${azimuthAngle.toInt()}°", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = enableGyro,
                        onCheckedChange = { enableGyro = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                    )
                }

                // Geometric Primitives Selector Row
                Text("Primitiva Geométrica de Guía:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Guide3DType.entries.toTypedArray()) { guide ->
                        val isSel = guide == selectedGuideType
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) ArNeonCyan else StudioDarkCard,
                            modifier = Modifier.clickable { selectedGuideType = guide }
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

                // Toggles Row (Camera AR vs Studio 3D, Wireframe, Proportions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Camera, contentDescription = null, tint = ArNeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Superposición AR Cámara", fontSize = 12.sp, color = Color.White)
                    }
                    Switch(
                        checked = isCameraArMode,
                        onCheckedChange = { isCameraArMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArNeonCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Líneas de Proporción / Cuadrícula", fontSize = 12.sp, color = Color.White)
                    }
                    Switch(
                        checked = showProportions,
                        onCheckedChange = { showProportions = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArNeonGold)
                    )
                }

                // Capture Progress Button
                Button(
                    onClick = {
                        takeCameraPhoto(
                            context = context,
                            imageCapture = activeImageCapture,
                            onPhotoCaptured = { savedUri ->
                                viewModel.addTimelapseSnapshot(savedUri.toString(), stageLabel = "Primitiva 3D: ${selectedGuideType.displayName}")
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
