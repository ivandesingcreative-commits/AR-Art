package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String = "",
    val targetTimestamp: Long,
    val isCompleted: Boolean = false,
    val completedTimestamp: Long? = null,
    val reminderEnabled: Boolean = true,
    val stageType: String = "MODELADO" // MODELADO, SECADO_INICIAL, LIJADO, DETALLES, PINTURA, BARNIZ
)
