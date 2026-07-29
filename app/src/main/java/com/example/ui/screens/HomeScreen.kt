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
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.material.icons.filled.FolderOpen
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
    onNavigateToTimelapse: () -> Unit,
    onNavigateToProjectDetail: (Long) -> Unit,
    onNavigateToCloudSync: () -> Unit,
    onNavigateToPrintableMarkers: () -> Unit,
    onNavigateToFileExplorer: () -> Unit = {}
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
                            "Estudio de Escultura y Superficies 2D / 3D",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToFileExplorer,
                        modifier = Modifier.testTag("btn_file_explorer_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Explorador de Archivos y Carpetas",
                            tint = ArNeonCyan
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Studio Hero Card (DIRECT ACCESS WITHOUT FORCED PROJECT CREATION)
            item {
                StudioHeroCard(onQuickLightbox = {
                    viewModel.ensureDefaultProject {
                        onNavigateToLightbox()
                    }
                })
            }

            // Core Studio Tools Action Grid
            item {
                Text(
                    "Herramientas de Estudio",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolActionCard(
                        title = "Mesa de Luz AR",
                        subtitle = "Superposición & Transparencia",
                        icon = Icons.Default.Lightbulb,
                        accentColor = ArNeonGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_lightbox"),
                        onClick = {
                            viewModel.ensureDefaultProject {
                                onNavigateToLightbox()
                            }
                        }
                    )
                    ToolActionCard(
                        title = "Guías 3D & AR",
                        subtitle = "Anatomía & Primitivas",
                        icon = Icons.Default.ViewInAr,
                        accentColor = ArNeonCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_ar"),
                        onClick = {
                            viewModel.ensureDefaultProject {
                                onNavigateToAr()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolActionCard(
                        title = "Galería de Avances",
                        subtitle = "Capturas & Comparativas",
                        icon = Icons.Default.PhotoCamera,
                        accentColor = TerracottaPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_timelapse"),
                        onClick = {
                            viewModel.ensureDefaultProject {
                                onNavigateToTimelapse()
                            }
                        }
                    )
                    ToolActionCard(
                        title = "Marcadores & QR",
                        subtitle = "Imprimir Hoja Ejes X,Y,Z",
                        icon = Icons.Default.QrCode2,
                        accentColor = ArNeonGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_tool_printable_markers"),
                        onClick = onNavigateToPrintableMarkers
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
                        "Mis Proyectos Escultóricos",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { showNewProjectDialog = true }) {
                        Text("+ Crear Proyecto", color = TerracottaPrimary)
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
            onCreate = { title, cat, projType, thickness, notes ->
                viewModel.createProject(title, cat, projType, thickness, notes)
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
                            TerracottaPrimary.copy(alpha = 0.28f),
                            ArNeonCyan.copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.25f),
                        modifier = Modifier.size(46.dp)
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
                            "Mesa de Luz & Referencias",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Compara frente, lado y atrás con opacidad regulable en cámara",
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
                    Text("Abrir Mesa de Luz Ahora", fontWeight = FontWeight.Bold)
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
                color = accentColor.copy(alpha = 0.18f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "La arcilla requiere secado uniforme a temperatura ambiente. Registra tus fotos para evaluar la evolución.",
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
                "Aún no tienes proyectos creados",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Puedes usar la Mesa de Luz directamente o crear un proyecto organizado.",
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
                        "${project.category} • ${if (project.projectType == "PLANO") "Plano / 2D" else "Figura / Escultura 3D"}",
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
    onCreate: (title: String, category: String, projectType: String, thickness: Float, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Busto / Personaje") }
    var projectType by remember { mutableStateOf("FIGURA") } // FIGURA (3D) or PLANO (2D)
    var thicknessMm by remember { mutableFloatStateOf(15f) }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Busto / Personaje", "Forma / Objeto Geométrico", "Vasija / Cuenco", "Miniatura", "Cuadro / Relieve", "Otro")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Text("Nuevo Proyecto del Artista", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del Proyecto / Obra") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_title")
                )

                Text("Tipo de Proyecto:", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isFigura = projectType == "FIGURA"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFigura) TerracottaPrimary else StudioDarkCard,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { projectType = "FIGURA" }
                    ) {
                        Text(
                            "Figura / Escultura (3D)",
                            fontSize = 11.sp,
                            color = if (isFigura) Color.White else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }

                    val isPlano = projectType == "PLANO"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPlano) ArNeonCyan else StudioDarkCard,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { projectType = "PLANO" }
                    ) {
                        Text(
                            "Plano / Lienzo (2D)",
                            fontSize = 11.sp,
                            color = if (isPlano) Color.Black else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }
                }

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
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de diseño o referencias") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, category, projectType, thicknessMm, notes) },
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
