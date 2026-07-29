package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val pendingReminders: Flow<List<MilestoneEntity>> = dao.getPendingReminders()

    fun getProject(id: Long): Flow<ProjectEntity?> = dao.getProjectByIdFlow(id)

    fun getReferencePhotos(projectId: Long): Flow<List<ReferencePhotoEntity>> =
        dao.getReferencePhotosForProject(projectId)

    fun getMilestones(projectId: Long): Flow<List<MilestoneEntity>> =
        dao.getMilestonesForProject(projectId)

    fun getTimelapseFrames(projectId: Long): Flow<List<TimelapseFrameEntity>> =
        dao.getTimelapseFramesForProject(projectId)

    suspend fun saveProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        if (project.id == 0L) {
            val newId = dao.insertProject(project)
            // Automatically add default milestones for air dry clay sculpting
            val now = System.currentTimeMillis()
            val dryHours = project.dryingHoursNeeded
            dao.insertMilestone(
                MilestoneEntity(
                    projectId = newId,
                    title = "Completar Estructura Base",
                    description = "Formar la masa principal de arcilla de secado al aire",
                    targetTimestamp = now + (2 * 3600 * 1000),
                    stageType = "MODELADO"
                )
            )
            dao.insertMilestone(
                MilestoneEntity(
                    projectId = newId,
                    title = "Detalles Superficiales y Alisado",
                    description = "Alisar grietas con pincel húmedo o agua",
                    targetTimestamp = now + (4 * 3600 * 1000),
                    stageType = "DETALLES"
                )
            )
            dao.insertMilestone(
                MilestoneEntity(
                    projectId = newId,
                    title = "Comienzo del Secado Natural",
                    description = "Dejar secar a temperatura ambiente lejos de luz directa",
                    targetTimestamp = now + (5 * 3600 * 1000),
                    stageType = "SECANDO"
                )
            )
            dao.insertMilestone(
                MilestoneEntity(
                    projectId = newId,
                    title = "Verificación de Dureza y Lijado Suave",
                    description = "Secado completo estimado (${dryHours}h). Lijar asperezas.",
                    targetTimestamp = now + (dryHours * 3600 * 1000L),
                    stageType = "LIJADO"
                )
            )
            dao.insertMilestone(
                MilestoneEntity(
                    projectId = newId,
                    title = "Pintado y Barnizado Protector",
                    description = "Aplicar acrílicos y sellador impermeable",
                    targetTimestamp = now + ((dryHours + 12) * 3600 * 1000L),
                    stageType = "PINTURA"
                )
            )
            newId
        } else {
            dao.updateProject(project)
            project.id
        }
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        dao.deleteProject(project)
    }

    suspend fun addReferencePhoto(photo: ReferencePhotoEntity) = withContext(Dispatchers.IO) {
        dao.insertReferencePhoto(photo)
    }

    suspend fun deleteReferencePhoto(photo: ReferencePhotoEntity) = withContext(Dispatchers.IO) {
        dao.deleteReferencePhoto(photo)
    }

    suspend fun addMilestone(milestone: MilestoneEntity) = withContext(Dispatchers.IO) {
        dao.insertMilestone(milestone)
    }

    suspend fun updateMilestone(milestone: MilestoneEntity) = withContext(Dispatchers.IO) {
        dao.updateMilestone(milestone)
    }

    suspend fun deleteMilestone(milestone: MilestoneEntity) = withContext(Dispatchers.IO) {
        dao.deleteMilestone(milestone)
    }

    suspend fun addTimelapseFrame(frame: TimelapseFrameEntity) = withContext(Dispatchers.IO) {
        dao.insertTimelapseFrame(frame)
    }

    // Cloud Sync simulation
    suspend fun syncAllToCloud(): Boolean = withContext(Dispatchers.IO) {
        // Simulate network roundtrip and mark projects as synced
        kotlinx.coroutines.delay(1200)
        // In real backend, Retrofit or Firestore syncs here
        true
    }
}
