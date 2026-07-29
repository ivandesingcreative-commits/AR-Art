package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class OrientationSensorHelper(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    var azimuthDegrees by mutableFloatStateOf(0f)
    var pitchDegrees by mutableFloatStateOf(0f)
    var rollDegrees by mutableFloatStateOf(0f)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun startListening() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert radians to degrees
        var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        if (azimuth < 0) azimuth += 360f

        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        azimuthDegrees = azimuth
        pitchDegrees = pitch
        rollDegrees = roll
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun rememberOrientationDegrees(enabled: Boolean): Float {
    val context = LocalContext.current
    val sensorHelper = remember { OrientationSensorHelper(context) }

    DisposableEffect(enabled) {
        if (enabled) {
            sensorHelper.startListening()
        } else {
            sensorHelper.stopListening()
        }
        onDispose {
            sensorHelper.stopListening()
        }
    }

    return sensorHelper.azimuthDegrees
}
