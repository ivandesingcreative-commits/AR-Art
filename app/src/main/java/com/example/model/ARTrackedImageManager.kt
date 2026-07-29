package com.example.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class MarkerType(val displayName: String, val symbol: String) {
    CIRCLE_X("Círculo (Eje X / Frente)", "⚪"),
    TRIANGLE_Z("Triángulo (Eje Z / Perfil)", "🔺"),
    CROSS_Y("Cruz (Eje Y / Centro)", "✚"),
    QR_SLOT("Código QR de Posición", "🔳")
}

data class Pose3D(
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val rotX: Float = 0f,
    val rotY: Float = 0f,
    val rotZ: Float = 0f
)

data class ReferenceSlot(
    val slotId: Int,
    val title: String,
    val targetAngle: Float,
    val markerType: MarkerType,
    val qrCodeId: String,
    var imageUri: String? = null,
    val relativePose: Pose3D = Pose3D(),
    val relativeScale: Float = 1.0f,
    var isTracked: Boolean = false
)

class ARTrackedImageManager {

    var activeSlotId by mutableStateOf(1)
    var primaryAnchorId by mutableStateOf("QR_SLOT_1")

    var rootPositionX by mutableFloatStateOf(0f)
    var rootPositionY by mutableFloatStateOf(0f)
    var rootRotationY by mutableFloatStateOf(0f)
    var rootScale by mutableFloatStateOf(1.0f)

    var isTrackingActive by mutableStateOf(false)
    var detectedMarkerName by mutableStateOf<String?>(null)

    val slots = mutableListOf(
        ReferenceSlot(1, "Frente (0°)", 0f, MarkerType.CIRCLE_X, "QR_SLOT_1"),
        ReferenceSlot(2, "3/4 Frontal Izq (45°)", 45f, MarkerType.QR_SLOT, "QR_SLOT_2"),
        ReferenceSlot(3, "Perfil Izquierdo (90°)", 90f, MarkerType.TRIANGLE_Z, "QR_SLOT_3"),
        ReferenceSlot(4, "3/4 Trasero Izq (135°)", 135f, MarkerType.QR_SLOT, "QR_SLOT_4"),
        ReferenceSlot(5, "Atrás / Reverso (180°)", 180f, MarkerType.CROSS_Y, "QR_SLOT_5"),
        ReferenceSlot(6, "3/4 Trasero Der (225°)", 225f, MarkerType.QR_SLOT, "QR_SLOT_6"),
        ReferenceSlot(7, "Perfil Derecho (270°)", 270f, MarkerType.TRIANGLE_Z, "QR_SLOT_7"),
        ReferenceSlot(8, "3/4 Frontal Der (315°)", 315f, MarkerType.QR_SLOT, "QR_SLOT_8"),
        ReferenceSlot(9, "Vista Superior (Zenith)", 0f, MarkerType.CROSS_Y, "QR_SLOT_9")
    )

    fun onTrackedImagesChanged(markerName: String, offsetX: Float, offsetY: Float, rotY: Float) {
        detectedMarkerName = markerName
        isTrackingActive = true
        rootPositionX = offsetX
        rootPositionY = offsetY
        rootRotationY = rotY

        // Auto-snap active slot based on detected marker
        val matchedSlot = slots.find { it.qrCodeId == markerName || it.title.lowercase().contains(markerName.lowercase()) }
        if (matchedSlot != null) {
            activeSlotId = matchedSlot.slotId
            primaryAnchorId = matchedSlot.qrCodeId
        }
    }

    fun getActiveSlot(): ReferenceSlot {
        return slots.find { it.slotId == activeSlotId } ?: slots[0]
    }
}
