package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY createdTimestamp DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Reference Photos
    @Query("SELECT * FROM reference_photos WHERE projectId = :projectId ORDER BY addedTimestamp ASC")
    fun getReferencePhotosForProject(projectId: Long): Flow<List<ReferencePhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferencePhoto(photo: ReferencePhotoEntity): Long

    @Delete
    suspend fun deleteReferencePhoto(photo: ReferencePhotoEntity)

    // Milestones
    @Query("SELECT * FROM milestones WHERE projectId = :projectId ORDER BY targetTimestamp ASC")
    fun getMilestonesForProject(projectId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE reminderEnabled = 1 AND isCompleted = 0")
    fun getPendingReminders(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    // Timelapse Frames
    @Query("SELECT * FROM timelapse_frames WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getTimelapseFramesForProject(projectId: Long): Flow<List<TimelapseFrameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelapseFrame(frame: TimelapseFrameEntity): Long

    @Delete
    suspend fun deleteTimelapseFrame(frame: TimelapseFrameEntity)

    @Query("DELETE FROM timelapse_frames WHERE projectId = :projectId")
    suspend fun clearTimelapseFrames(projectId: Long)
}
