package com.escom.ipn.p4.filemanager.model

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

enum class SensorType(val displayName: String, val unit: String) {
    ACCELEROMETER("Acelerómetro", "m/s²"),
    GYROSCOPE("Giroscopio", "rad/s")
}

enum class SensorStatus {
    ACTIVE,
    PAUSED,
    UNAVAILABLE
}

@Serializable
data class SensorData(
    val type: SensorType,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val unit: String = type.unit,
    val timestampMillis: Long = 0L,
    val accuracy: Int = 3,
    val isSimulated: Boolean = false,
    val status: SensorStatus = SensorStatus.ACTIVE
)

data class MotionTiltState(
    val pitch: Float = 0f, // Inclinación adelante/atrás en grados
    val roll: Float = 0f,  // Inclinación lateral izquierda/derecha en grados
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f
)

interface ISensorManager {
    val accelerometerFlow: StateFlow<SensorData>
    val gyroscopeFlow: StateFlow<SensorData>
    val shakeEventFlow: StateFlow<Long> // Timestamp del último shake detectado
    val tiltStateFlow: StateFlow<MotionTiltState>
    
    fun isSensorAvailable(type: SensorType): Boolean
    fun startListening(samplingPeriodMs: Long = 60L)
    fun stopListening()
    fun setSimulationMode(enabled: Boolean)
    fun injectSimulatedReading(type: SensorType, x: Float, y: Float, z: Float)
}
