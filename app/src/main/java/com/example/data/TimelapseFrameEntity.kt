package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timelapse_frames")
data class TimelapseFrameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val imageUri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stageLabel: String = "Etapa de modelado",
    val isKeyframe: Boolean = false
)
