package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Escultura de Arcilla", // Head / Bust, Animal, Miniature, Vase, Custom
    val projectType: String = "FIGURA", // FIGURA (3D / Escultura) or PLANO (2D / Lienzo / Dibujo)
    val clayThicknessMm: Float = 15f, // Thickness in mm for drying calculation
    val createdTimestamp: Long = System.currentTimeMillis(),
    val status: String = "EN_PROCESO", // EN_PROCESO, SECANDO, LISTO_PARA_PINTAR, COMPLETADO
    val coverImageUri: String? = null,
    val notes: String = "",
    val dryingHoursNeeded: Int = 24,
    val dryingStartedTimestamp: Long? = null,
    val cloudSynced: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
