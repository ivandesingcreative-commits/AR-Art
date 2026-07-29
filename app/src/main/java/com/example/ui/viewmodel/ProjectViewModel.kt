package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MilestoneEntity
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.data.ReferencePhotoEntity
import com.example.data.TimelapseFrameEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    val projects: StateFlow<List<ProjectEntity>>
    val pendingReminders: StateFlow<List<MilestoneEntity>>

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    val currentProject: StateFlow<ProjectEntity?>
    val referencePhotos: StateFlow<List<ReferencePhotoEntity>>
    val milestones: StateFlow<List<MilestoneEntity>>
    val timelapseFrames: StateFlow<List<TimelapseFrameEntity>>

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _selectedReferencePhoto = MutableStateFlow<ReferencePhotoEntity?>(null)
    val selectedReferencePhoto: StateFlow<ReferencePhotoEntity?> = _selectedReferencePhoto.asStateFlow()

    // Lightbox Live Controls
    val overlayOpacity = MutableStateFlow(0.5f)
    val isTraceContourMode = MutableStateFlow(false)
    val traceThreshold = MutableStateFlow(0.3f)
    val overlayScale = MutableStateFlow(1.0f)
    val overlayRotation = MutableStateFlow(0f)
    val isFlippedHorizontally = MutableStateFlow(false)

    init {
        val dao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(dao)

        projects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingReminders = repository.pendingReminders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        currentProject = _selectedProjectId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getProject(id)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        referencePhotos = _selectedProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getReferencePhotos(id)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        milestones = _selectedProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMilestones(id)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        timelapseFrames = _selectedProjectId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getTimelapseFrames(id)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun selectProject(projectId: Long?) {
        _selectedProjectId.value = projectId
    }

    fun ensureDefaultProject(onReady: (Long) -> Unit) {
        val currentId = _selectedProjectId.value
        if (currentId != null) {
            onReady(currentId)
            return
        }
        viewModelScope.launch {
            val list = projects.value
            if (list.isNotEmpty()) {
                val existingId = list.first().id
                _selectedProjectId.value = existingId
                onReady(existingId)
            } else {
                val defaultProject = ProjectEntity(
                    title = "Estudio Libre",
                    category = "Escultura de Arcilla",
                    projectType = "FIGURA",
                    clayThicknessMm = 15f,
                    notes = "Sesión rápida de mesa de luz y referencias"
                )
                val newId = repository.saveProject(defaultProject)
                _selectedProjectId.value = newId
                onReady(newId)
            }
        }
    }

    fun selectReferencePhoto(photo: ReferencePhotoEntity?) {
        _selectedReferencePhoto.value = photo
        photo?.let {
            overlayOpacity.value = it.defaultOpacity
            overlayScale.value = it.scaleX
            overlayRotation.value = it.rotationDegrees
            isTraceContourMode.value = it.isTraceMode
        }
    }

    fun createProject(
        title: String,
        category: String,
        projectType: String = "FIGURA",
        clayThicknessMm: Float,
        notes: String
    ) {
        viewModelScope.launch {
            val dryingHours = ((clayThicknessMm / 10f) * 24f).coerceIn(12f, 96f).toInt()
            val newProject = ProjectEntity(
                title = title.ifBlank { "Proyecto de Arcilla" },
                category = category,
                projectType = projectType,
                clayThicknessMm = clayThicknessMm,
                notes = notes,
                dryingHoursNeeded = dryingHours
            )
            val newId = repository.saveProject(newProject)
            _selectedProjectId.value = newId
        }
    }

    fun addReferencePhoto(uri: Uri, viewName: String = "Frente", angle: String = "FRONTAL") {
        ensureDefaultProject { projId ->
            viewModelScope.launch {
                val photo = ReferencePhotoEntity(
                    projectId = projId,
                    imageUri = uri.toString(),
                    title = viewName,
                    angle = angle,
                    defaultOpacity = overlayOpacity.value
                )
                val newPhotoId = repository.addReferencePhoto(photo)
                val createdPhoto = photo.copy(id = newPhotoId)
                _selectedReferencePhoto.value = createdPhoto
            }
        }
    }

    fun addReferencePhotoUriString(uriString: String, viewName: String = "Frente", angle: String = "FRONTAL") {
        ensureDefaultProject { projId ->
            viewModelScope.launch {
                val photo = ReferencePhotoEntity(
                    projectId = projId,
                    imageUri = uriString,
                    title = viewName,
                    angle = angle,
                    defaultOpacity = overlayOpacity.value
                )
                val newPhotoId = repository.addReferencePhoto(photo)
                val createdPhoto = photo.copy(id = newPhotoId)
                _selectedReferencePhoto.value = createdPhoto
            }
        }
    }

    fun deleteReferencePhoto(photo: ReferencePhotoEntity) {
        viewModelScope.launch {
            repository.deleteReferencePhoto(photo)
            if (_selectedReferencePhoto.value?.id == photo.id) {
                _selectedReferencePhoto.value = null
            }
        }
    }

    fun toggleMilestoneCompleted(milestone: MilestoneEntity) {
        viewModelScope.launch {
            val updated = milestone.copy(
                isCompleted = !milestone.isCompleted,
                completedTimestamp = if (!milestone.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateMilestone(updated)
        }
    }

    fun addTimelapseSnapshot(imageUri: String, stageLabel: String = "Avance de escultura") {
        ensureDefaultProject { projId ->
            viewModelScope.launch {
                repository.addTimelapseFrame(
                    TimelapseFrameEntity(
                        projectId = projId,
                        imageUri = imageUri,
                        timestamp = System.currentTimeMillis(),
                        stageLabel = stageLabel
                    )
                )
            }
        }
    }

    fun deleteTimelapseFrame(frame: TimelapseFrameEntity) {
        viewModelScope.launch {
            repository.deleteTimelapseFrame(frame)
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncAllToCloud()
            _isSyncing.value = false
        }
    }
}
