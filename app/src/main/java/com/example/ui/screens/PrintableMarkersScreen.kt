package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ARTrackedImageManager
import com.example.model.MarkerType
import com.example.model.ReferenceSlot
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintableMarkersScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { ARTrackedImageManager() }
    var selectedSlotForPreview by remember { mutableStateOf<ReferenceSlot?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Marcadores y Códigos QR (AR)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Anclajes físicos 9 posiciones • Ejes X, Y, Z",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_printable_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "🖨️ Generando Hoja de Marcadores PDF...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Imprimir Hoja", tint = ArNeonGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkSurface.copy(alpha = 0.95f)
                )
            )
        },
        containerColor = StudioDarkSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Instructions Card
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioDarkCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Instrucciones de Anclaje Rígido Mesa/Plato Giratorio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        "Para que la superposición AR permanezca anclada sin desplazarse cuando muevas el móvil o gires la pieza, imprime o dibuja estos marcadores de alto contraste en tu base de trabajo:\n" +
                                "• ⚪ Círculo: Eje X (Frente 0°)\n" +
                                "• 🔺 Triángulo: Eje Z (Perfil 90°)\n" +
                                "• ✚ Cruz: Eje Y (Centro / Atrás)\n" +
                                "• 🔳 Códigos QR (Slots 1 al 9): Anclaje automático de multivista.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "✨ Hoja de marcadores lista para imprimir en A4", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.testTag("btn_export_qr_pdf")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir / Imprimir Hoja A4", fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                "Biblioteca de 9 Anclajes Espaciales",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )

            // 9 Slot Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(manager.slots) { slot ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioDarkCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ArNeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            // Marker Drawing Visual Canvas
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                MarkerGraphicCanvas(markerType = slot.markerType, slotNumber = slot.slotId)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                slot.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                slot.markerType.displayName.split(" ").first(),
                                color = ArNeonGold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkerGraphicCanvas(markerType: MarkerType, slotNumber: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // High contrast outer frame
        drawRect(color = Color.Black, size = size, style = Stroke(width = 4f))

        when (markerType) {
            MarkerType.CIRCLE_X -> {
                drawCircle(color = Color.Black, radius = w * 0.35f, center = Offset(cx, cy), style = Stroke(width = 8f))
                drawCircle(color = Color.Black, radius = w * 0.12f, center = Offset(cx, cy))
            }
            MarkerType.TRIANGLE_Z -> {
                val path = Path().apply {
                    moveTo(cx, cy - h * 0.35f)
                    lineTo(cx - w * 0.35f, cy + h * 0.35f)
                    lineTo(cx + w * 0.35f, cy + h * 0.35f)
                    close()
                }
                drawPath(path = path, color = Color.Black, style = Stroke(width = 8f))
            }
            MarkerType.CROSS_Y -> {
                drawLine(color = Color.Black, start = Offset(cx - w * 0.35f, cy), end = Offset(cx + w * 0.35f, cy), strokeWidth = 10f)
                drawLine(color = Color.Black, start = Offset(cx, cy - h * 0.35f), end = Offset(cx, cy + h * 0.35f), strokeWidth = 10f)
            }
            MarkerType.QR_SLOT -> {
                // QR Finder Patterns (Top-Left, Top-Right, Bottom-Left)
                val qrBoxSize = w * 0.22f

                // TL
                drawRect(color = Color.Black, topLeft = Offset(w * 0.12f, h * 0.12f), size = Size(qrBoxSize, qrBoxSize))
                // TR
                drawRect(color = Color.Black, topLeft = Offset(w * 0.65f, h * 0.12f), size = Size(qrBoxSize, qrBoxSize))
                // BL
                drawRect(color = Color.Black, topLeft = Offset(w * 0.12f, h * 0.65f), size = Size(qrBoxSize, qrBoxSize))

                // Center Pattern dot grid
                drawCircle(color = Color.Black, radius = w * 0.08f, center = Offset(cx, cy))
            }
        }
    }
}
