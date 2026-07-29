package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reference_photos")
data class ReferencePhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val imageUri: String,
    val title: String = "Vista de referencia",
    val angle: String = "FRONTAL", // FRONTAL, LATERAL, TRES_CUARTOS, SUPERIOR, DETALLE
    val defaultOpacity: Float = 0.5f,
    val defaultThreshold: Float = 0f, // Trace contour filter threshold
    val isTraceMode: Boolean = false,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationDegrees: Float = 0f,
    val addedTimestamp: Long = System.currentTimeMillis()
)
