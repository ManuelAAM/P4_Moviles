package com.escom.ipn.p4.filemanager.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.escom.ipn.p4.filemanager.model.ISensorManager
import com.escom.ipn.p4.filemanager.model.MotionTiltState
import com.escom.ipn.p4.filemanager.model.SensorData
import com.escom.ipn.p4.filemanager.model.SensorStatus
import com.escom.ipn.p4.filemanager.model.SensorType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

class AndroidSensorManager(
    private val context: Context
) : ISensorManager, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _accelerometerFlow = MutableStateFlow(
        SensorData(
            type = SensorType.ACCELEROMETER,
            status = if (accelerometerSensor != null) SensorStatus.PAUSED else SensorStatus.UNAVAILABLE
        )
    )
    override val accelerometerFlow: StateFlow<SensorData> = _accelerometerFlow.asStateFlow()

    private val _gyroscopeFlow = MutableStateFlow(
        SensorData(
            type = SensorType.GYROSCOPE,
            status = if (gyroscopeSensor != null) SensorStatus.PAUSED else SensorStatus.UNAVAILABLE
        )
    )
    override val gyroscopeFlow: StateFlow<SensorData> = _gyroscopeFlow.asStateFlow()

    private val _shakeEventFlow = MutableStateFlow(0L)
    override val shakeEventFlow: StateFlow<Long> = _shakeEventFlow.asStateFlow()

    private val _tiltStateFlow = MutableStateFlow(MotionTiltState())
    override val tiltStateFlow: StateFlow<MotionTiltState> = _tiltStateFlow.asStateFlow()

    private var isListening = false
    private var isSimulationMode = false
    private var lastShakeTimestamp = 0L
    private var lastAccelUiTimestamp = 0L
    private var lastGyroUiTimestamp = 0L
    private var lastTiltUiTimestamp = 0L

    // Filtros pasa-bajas para estabilidad de visualización en pantalla
    private var smoothedAccelX = 0f
    private var smoothedAccelY = 9.8f
    private var smoothedAccelZ = 0f

    private var smoothedGyroX = 0f
    private var smoothedGyroY = 0f
    private var smoothedGyroZ = 0f

    override fun isSensorAvailable(type: SensorType): Boolean {
        return when (type) {
            SensorType.ACCELEROMETER -> accelerometerSensor != null
            SensorType.GYROSCOPE -> gyroscopeSensor != null
        }
    }

    override fun startListening(samplingPeriodMs: Long) {
        if (isListening || isSimulationMode) return
        val delayUs = (samplingPeriodMs * 1000).toInt().coerceIn(20000, 200000)

        accelerometerSensor?.let {
            sensorManager?.registerListener(this, it, delayUs)
            _accelerometerFlow.value = _accelerometerFlow.value.copy(status = SensorStatus.ACTIVE)
        }
        gyroscopeSensor?.let {
            sensorManager?.registerListener(this, it, delayUs)
            _gyroscopeFlow.value = _gyroscopeFlow.value.copy(status = SensorStatus.ACTIVE)
        }
        isListening = true
    }

    override fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        _accelerometerFlow.value = _accelerometerFlow.value.copy(status = SensorStatus.PAUSED)
        _gyroscopeFlow.value = _gyroscopeFlow.value.copy(status = SensorStatus.PAUSED)
        isListening = false
    }

    override fun setSimulationMode(enabled: Boolean) {
        isSimulationMode = enabled
        if (enabled) {
            stopListening()
        } else {
            startListening()
        }
    }

    override fun injectSimulatedReading(type: SensorType, x: Float, y: Float, z: Float) {
        val mag = sqrt(x * x + y * y + z * z)
        val reading = SensorData(
            type = type,
            x = x,
            y = y,
            z = z,
            magnitude = mag,
            timestampMillis = System.currentTimeMillis(),
            accuracy = 3,
            isSimulated = true,
            status = SensorStatus.ACTIVE
        )

        when (type) {
            SensorType.ACCELEROMETER -> {
                _accelerometerFlow.value = reading
                processMotion(x, y, z, mag, reading.timestampMillis)
            }
            SensorType.GYROSCOPE -> {
                _gyroscopeFlow.value = reading
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || isSimulationMode) return
        val now = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = sqrt(x * x + y * y + z * z)

                // 1. Detección inmediata de agitación con aceleración bruta sin retraso
                if (mag > 13.5f && (now - lastShakeTimestamp > 650L)) {
                    lastShakeTimestamp = now
                    _shakeEventFlow.value = now
                }

                // 2. Filtro pasa-bajas para números estables y sin brincos visuales
                smoothedAccelX += 0.25f * (x - smoothedAccelX)
                smoothedAccelY += 0.25f * (y - smoothedAccelY)
                smoothedAccelZ += 0.25f * (z - smoothedAccelZ)
                val smoothedMag = sqrt(smoothedAccelX * smoothedAccelX + smoothedAccelY * smoothedAccelY + smoothedAccelZ * smoothedAccelZ)

                // 3. Emisión a UI con limitador de refresco (cada 120 ms = 8.3 fps estables)
                if (now - lastAccelUiTimestamp >= 120L) {
                    lastAccelUiTimestamp = now
                    _accelerometerFlow.value = SensorData(
                        type = SensorType.ACCELEROMETER,
                        x = smoothedAccelX,
                        y = smoothedAccelY,
                        z = smoothedAccelZ,
                        magnitude = smoothedMag,
                        timestampMillis = now,
                        accuracy = event.accuracy,
                        isSimulated = false,
                        status = SensorStatus.ACTIVE
                    )
                }

                // 4. Orientación panorámica suave (Tilt-to-Pan a ~20 fps)
                if (now - lastTiltUiTimestamp >= 50L) {
                    lastTiltUiTimestamp = now
                    processMotion(smoothedAccelX, smoothedAccelY, smoothedAccelZ, smoothedMag, now)
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Filtro pasa-bajas giroscopio
                smoothedGyroX += 0.25f * (x - smoothedGyroX)
                smoothedGyroY += 0.25f * (y - smoothedGyroY)
                smoothedGyroZ += 0.25f * (z - smoothedGyroZ)
                val smoothedMag = sqrt(smoothedGyroX * smoothedGyroX + smoothedGyroY * smoothedGyroY + smoothedGyroZ * smoothedGyroZ)

                if (now - lastGyroUiTimestamp >= 120L) {
                    lastGyroUiTimestamp = now
                    _gyroscopeFlow.value = SensorData(
                        type = SensorType.GYROSCOPE,
                        x = smoothedGyroX,
                        y = smoothedGyroY,
                        z = smoothedGyroZ,
                        magnitude = smoothedMag,
                        timestampMillis = now,
                        accuracy = event.accuracy,
                        isSimulated = false,
                        status = SensorStatus.ACTIVE
                    )
                }
            }
        }
    }

    private fun processMotion(x: Float, y: Float, z: Float, magnitude: Float, timestamp: Long) {
        // Detección de agitación (Shake)
        if (magnitude > 13.5f && (timestamp - lastShakeTimestamp > 650L)) {
            lastShakeTimestamp = timestamp
            _shakeEventFlow.value = timestamp
        }

        // Estimación de inclinación angular (Pitch & Roll)
        val pitch = (atan2(y.toDouble(), sqrt((x * x + z * z).toDouble())) * (180.0 / PI)).toFloat()
        val roll = (atan2(-x.toDouble(), z.toDouble()) * (180.0 / PI)).toFloat()

        // Offsets suaves para exploración panorámica (Tilt-to-Pan)
        val panX = (-roll * 5f).coerceIn(-250f, 250f)
        val panY = (pitch * 5f).coerceIn(-250f, 250f)

        _tiltStateFlow.value = MotionTiltState(
            pitch = pitch,
            roll = roll,
            panOffsetX = panX,
            panOffsetY = panY
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Manejo de cambio de precisión si se requiere
    }
}
