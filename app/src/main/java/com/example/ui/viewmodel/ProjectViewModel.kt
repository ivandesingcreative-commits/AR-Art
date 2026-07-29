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
        clayThicknessMm: Float,
        notes: String
    ) {
        viewModelScope.launch {
            // Air dry clay takes approx 24h per 10mm thickness
            val dryingHours = ((clayThicknessMm / 10f) * 24f).coerceIn(12f, 96f).toInt()
            val newProject = ProjectEntity(
                title = title.ifBlank { "Proyecto de Arcilla" },
                category = category,
                clayThicknessMm = clayThicknessMm,
                notes = notes,
                dryingHoursNeeded = dryingHours
            )
            val newId = repository.saveProject(newProject)
            _selectedProjectId.value = newId
        }
    }

    fun addReferencePhoto(uri: Uri, angle: String = "FRONTAL") {
        val id = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val photo = ReferencePhotoEntity(
                projectId = id,
                imageUri = uri.toString(),
                angle = angle,
                defaultOpacity = overlayOpacity.value
            )
            repository.addReferencePhoto(photo)
        }
    }

    fun addReferencePhotoUriString(uriString: String, angle: String = "FRONTAL") {
        val id = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val photo = ReferencePhotoEntity(
                projectId = id,
                imageUri = uriString,
                angle = angle,
                defaultOpacity = overlayOpacity.value
            )
            repository.addReferencePhoto(photo)
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

    fun addTimelapseSnapshot(imageUri: String) {
        val id = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.addTimelapseFrame(
                TimelapseFrameEntity(
                    projectId = id,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis()
                )
            )
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
