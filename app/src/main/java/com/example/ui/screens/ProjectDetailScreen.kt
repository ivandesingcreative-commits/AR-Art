package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.MilestoneEntity
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.ClayAmber
import com.example.ui.theme.ClaySage
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onOpenLightbox: () -> Unit,
    onOpenAr: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val referencePhotos by viewModel.referencePhotos.collectAsState()
    val milestones by viewModel.milestones.collectAsState()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addReferencePhoto(it, "FRONTAL")
            Toast.makeText(context, "Foto de referencia agregada al proyecto", Toast.LENGTH_SHORT).show()
        }
    }

    val project = currentProject

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        project?.title ?: "Detalle del Proyecto",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_project_detail_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioDarkSurface)
            )
        },
        containerColor = StudioDarkBg
    ) { padding ->
        if (project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando proyecto...", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Project Summary Card & Drying Time
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        project.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        project.category,
                                        fontSize = 12.sp,
                                        color = ArNeonCyan
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TerracottaPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "${project.dryingHoursNeeded} Horas de Secado",
                                        color = TerracottaPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Air dry clay thickness stats
                            Text(
                                "Grosor de Arcilla: ${project.clayThicknessMm.toInt()} mm",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                            val completedCount = milestones.count { it.isCompleted }
                            val totalCount = milestones.size.coerceAtLeast(1)
                            val progressFloat = completedCount.toFloat() / totalCount.toFloat()

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Avance de Hitos de Escultura", fontSize = 12.sp, color = Color.White)
                                Text("${(progressFloat * 100).toInt()}%", fontSize = 12.sp, color = ClayAmber, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier.fillMaxWidth(),
                                color = ClayAmber,
                                trackColor = StudioDarkSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Launch Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onOpenLightbox,
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_detail_launch_lightbox")
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mesa de Luz", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onOpenAr,
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurface),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_detail_launch_ar")
                                ) {
                                    Text("Guía AR 3D", fontSize = 12.sp, color = ArNeonCyan)
                                }
                            }
                        }
                    }
                }

                // Reference Photos Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Fotos de Referencia (${referencePhotos.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { photoLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Subir Foto", tint = ArNeonCyan)
                        }
                    }

                    if (referencePhotos.isEmpty()) {
                        Text(
                            "Aún no has agregado fotos de referencia a este proyecto.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(referencePhotos) { photo ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = StudioDarkCard,
                                    modifier = Modifier
                                        .size(100.dp)
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
                    }
                }

                // Milestones & Push Notification Reminders Checklist
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hitos y Tiempos de Secado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Button(
                            onClick = {
                                NotificationHelper.sendMilestoneNotification(
                                    context,
                                    "Recordatorio de Secado",
                                    "Es tiempo de revisar la dureza y realizar el lijado suave en tu escultura ${project.title}."
                                )
                                Toast.makeText(context, "🔔 Notificación push enviada", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ArNeonGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Probar Push", fontSize = 11.sp, color = ArNeonGold)
                        }
                    }
                }

                items(milestones) { milestone ->
                    MilestoneCardItem(
                        milestone = milestone,
                        onToggle = { viewModel.toggleMilestoneCompleted(milestone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneCardItem(
    milestone: MilestoneEntity,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = milestone.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = ClaySage, checkmarkColor = Color.Black)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    milestone.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (milestone.isCompleted) Color.Gray else Color.White
                )
                if (milestone.description.isNotBlank()) {
                    Text(
                        milestone.description,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = StudioDarkSurface
            ) {
                Text(
                    milestone.stageType,
                    fontSize = 10.sp,
                    color = ArNeonCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
