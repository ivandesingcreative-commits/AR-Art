package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.ClayAmber
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProjectViewModel,
    onNavigateToLightbox: () -> Unit,
    onNavigateToAr: () -> Unit,
    onNavigateToSlowMo: () -> Unit,
    onNavigateToTimelapse: () -> Unit,
    onNavigateToProjectDetail: (Long) -> Unit,
    onNavigateToCloudSync: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ClayStudio AR",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Mesa de Luz y Esculturas Air Dry Clay",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCloudSync,
                        modifier = Modifier.testTag("btn_cloud_sync_header")
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = "Estado Nube",
                            tint = if (isSyncing) ArNeonGold else ArNeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = TerracottaPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
            }
        },
        containerColor = StudioDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Studio Hero Card
            item {
                StudioHeroCard(onQuickLightbox = {
                    if (projects.isNotEmpty() && selectedProjectId == null) {
                        viewModel.selectProject(projects.first().id)
                    }
                    onNavigateToLightbox()
                })
            }

            // Core Studio Tools Action Grid
            item {
                Text(
                    "Herramientas de Estudio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolActionCard(
                        title = "Mesa de Luz",
                        subtitle = "Superposición de fotos",
                        icon = Icons.Default.Lightbulb,
                        accentColor = ArNeonGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_lightbox"),
                        onClick = {
                            if (projects.isNotEmpty() && selectedProjectId == null) {
                                viewModel.selectProject(projects.first().id)
                            }
                            onNavigateToLightbox()
                        }
                    )
                    ToolActionCard(
                        title = "Realidad AR",
                        subtitle = "Guías 3D de Arcilla",
                        icon = Icons.Default.ViewInAr,
                        accentColor = ArNeonCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_ar"),
                        onClick = {
                            if (projects.isNotEmpty() && selectedProjectId == null) {
                                viewModel.selectProject(projects.first().id)
                            }
                            onNavigateToAr()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolActionCard(
                        title = "Cámara Lenta",
                        subtitle = "Inspección de detalles",
                        icon = Icons.Default.SlowMotionVideo,
                        accentColor = Color(0xFFFF7043),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_slowmo"),
                        onClick = onNavigateToSlowMo
                    )
                    ToolActionCard(
                        title = "Timelapse",
                        subtitle = "Captura de avances",
                        icon = Icons.Default.PhotoCamera,
                        accentColor = Color(0xFFAB47BC),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_timelapse"),
                        onClick = {
                            if (projects.isNotEmpty() && selectedProjectId == null) {
                                viewModel.selectProject(projects.first().id)
                            }
                            onNavigateToTimelapse()
                        }
                    )
                }
            }

            // Air Dry Clay Drying Status Widget
            item {
                AirDryClayDryingWidget()
            }

            // Projects List Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mis Proyectos de Arcilla",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { showNewProjectDialog = true }) {
                        Text("+ Crear", color = TerracottaPrimary)
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    EmptyProjectsCard(onCreateClicked = { showNewProjectDialog = true })
                }
            } else {
                items(projects) { proj ->
                    ProjectCardItem(
                        project = proj,
                        isSelected = proj.id == selectedProjectId,
                        onClick = {
                            viewModel.selectProject(proj.id)
                            onNavigateToProjectDetail(proj.id)
                        }
                    )
                }
            }
        }
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { title, cat, thickness, notes ->
                viewModel.createProject(title, cat, thickness, notes)
                showNewProjectDialog = false
            }
        )
    }
}

@Composable
private fun StudioHeroCard(onQuickLightbox: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            StudioDarkCard,
                            TerracottaPrimary.copy(alpha = 0.25f),
                            ArNeonCyan.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ArNeonGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Esculpe con Precisión AR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Proporciones reales en pantalla mientras esculpes",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onQuickLightbox,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_quick_lightbox")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Mesa de Luz Ahora")
                }
            }
        }
    }
}

@Composable
private fun ToolActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AirDryClayDryingWidget() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = ClayAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Calculadora de Secado Air Dry Clay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ClayAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        "24h / 10mm",
                        fontSize = 10.sp,
                        color = ClayAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "La arcilla de secado al aire requiere un secado uniforme sin calor directo para evitar grietas. Usa notificaciones push para tus hitos de lijado y pintado.",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
private fun EmptyProjectsCard(onCreateClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Details,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Aún no tienes proyectos de arcilla",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Crea tu primer modelo para superponer fotos de referencia y medir proporciones.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Button(
                onClick = onCreateClicked,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Text("Crear Proyecto")
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    project: ProjectEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioDarkCard.copy(alpha = 0.9f) else StudioDarkCard
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        project.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        "${project.category} • Grosor ${project.clayThicknessMm.toInt()} mm",
                        fontSize = 12.sp,
                        color = ArNeonCyan
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerracottaPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${project.dryingHoursNeeded}h secado",
                        fontSize = 11.sp,
                        color = TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (project.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    project.notes,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, category: String, thickness: Float, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Busto / Personaje") }
    var thicknessMm by remember { mutableFloatStateOf(15f) }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Busto / Personaje", "Figura de Animal", "Vasija / Cuenco", "Miniatura", "Otro")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Text("Nuevo Proyecto de Arcilla", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la Escultura") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_title")
                )
                Text("Categoría:", fontSize = 12.sp, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSel = cat == category
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) TerracottaPrimary else StudioDarkCard,
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                cat,
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        "Grosor máximo de arcilla: ${thicknessMm.toInt()} mm",
                        fontSize = 12.sp,
                        color = ArNeonCyan
                    )
                    Slider(
                        value = thicknessMm,
                        onValueChange = { thicknessMm = it },
                        valueRange = 5f..60f,
                        steps = 11
                    )
                    Text(
                        "Tiempo de secado estimado: ${((thicknessMm / 10f) * 24f).toInt()} horas",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de diseño o herramientas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, category, thicknessMm, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.testTag("btn_confirm_create_project")
            ) {
                Text("Crear Proyecto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
