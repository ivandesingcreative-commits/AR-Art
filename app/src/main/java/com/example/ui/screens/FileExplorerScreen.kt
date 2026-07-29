package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArNeonCyan
import com.example.ui.theme.ArNeonGold
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ProjectViewModel
import com.example.util.Obj3DFileParser
import java.io.File

data class LocalFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val is3DModel: Boolean = false,
    val isImage: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()

    var currentFolderPath by remember {
        mutableStateOf(context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath)
    }

    var selectedFolderUri by remember { mutableStateOf<String?>(currentFolderPath) }

    val fileList = remember(currentFolderPath) {
        val folder = File(currentFolderPath)
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.map { file ->
                val nameLower = file.name.lowercase()
                LocalFileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    sizeBytes = file.length(),
                    is3DModel = nameLower.endsWith(".obj") || nameLower.endsWith(".stl"),
                    isImage = nameLower.endsWith(".jpg") || nameLower.endsWith(".png") || nameLower.endsWith(".webp")
                )
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // SAF Folder Picker Launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            selectedFolderUri = it.toString()
            Toast.makeText(context, "Carpeta de proyecto designada: ${it.path}", Toast.LENGTH_LONG).show()
        }
    }

    // SAF File Import Launcher (.obj, .stl, images)
    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val fileName = fileUri.lastPathSegment ?: "archivo_importado"
            if (fileName.lowercase().endsWith(".obj") || fileName.lowercase().endsWith(".stl")) {
                val parsedMesh = Obj3DFileParser.parseObjFile(context, fileUri, fileName)
                if (parsedMesh != null) {
                    Toast.makeText(context, "✅ Modelo 3D ${parsedMesh.name} cargado (${parsedMesh.vertices.size} vértices)", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ No se pudo procesar el archivo .OBJ", Toast.LENGTH_SHORT).show()
                }
            } else {
                viewModel.addReferencePhoto(fileUri, "Vista Importada - $fileName")
                Toast.makeText(context, "✅ Imagen de referencia añadida al proyecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Explorador de Archivos y Carpetas",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Proyecto: ${currentProject?.title ?: "Predeterminado"}",
                            fontSize = 12.sp,
                            color = ArNeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_explorer_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioDarkSurface)
            )
        },
        containerColor = StudioDarkSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Folder Designation Banner
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
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ArNeonGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Carpeta de Trabajo Designada",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        selectedFolderUri ?: currentFolderPath,
                        fontSize = 11.sp,
                        color = ArNeonCyan,
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { folderPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_project_folder")
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Designar Carpeta", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { fileImportLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ArNeonCyan, RoundedCornerShape(20.dp))
                                .testTag("btn_import_custom_file")
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = ArNeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar Archivo (.OBJ / Imagen)", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Current Directory Path & Up button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Contenido del Directorio",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )

                val parent = File(currentFolderPath).parentFile
                if (parent != null && parent.exists()) {
                    Button(
                        onClick = { currentFolderPath = parent.absolutePath },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkCard)
                    ) {
                        Text("⬆ Subir Nivel", fontSize = 11.sp, color = ArNeonGold)
                    }
                }
            }

            // File List
            if (fileList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(StudioDarkCard, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Directorio vacío o sin archivos de modelo", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fileList) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudioDarkCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.isDirectory) {
                                        currentFolderPath = item.path
                                    } else if (item.is3DModel) {
                                        val uri = Uri.fromFile(File(item.path))
                                        val parsed = Obj3DFileParser.parseObjFile(context, uri, item.name)
                                        if (parsed != null) {
                                            Toast.makeText(context, "✅ Modelo 3D ${parsed.name} leído correctamente", Toast.LENGTH_LONG).show()
                                        }
                                    } else if (item.isImage) {
                                        viewModel.addReferencePhoto(Uri.fromFile(File(item.path)), item.name)
                                        Toast.makeText(context, "✅ Añadido como foto de referencia", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when {
                                    item.isDirectory -> Icons.Default.Folder
                                    item.is3DModel -> Icons.Default.ModelTraining
                                    item.isImage -> Icons.Default.Image
                                    else -> Icons.Default.Description
                                }
                                val iconTint = when {
                                    item.isDirectory -> ArNeonGold
                                    item.is3DModel -> ArNeonCyan
                                    item.isImage -> TerracottaPrimary
                                    else -> Color.LightGray
                                }

                                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        if (item.isDirectory) "Carpeta" else "${item.sizeBytes / 1024} KB",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }

                                if (item.is3DModel) {
                                    Text("3D OBJ", color = ArNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
