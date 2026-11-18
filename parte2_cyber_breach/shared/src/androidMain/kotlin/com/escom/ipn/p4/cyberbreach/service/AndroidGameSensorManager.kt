package com.escom.ipn.p4.cyberbreach.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.escom.ipn.p4.cyberbreach.model.IGameSensorListener
import com.escom.ipn.p4.cyberbreach.model.IGameSensorManager
import kotlin.math.sqrt

class AndroidGameSensorManager(context: Context) : IGameSensorManager, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var listener: IGameSensorListener? = null
    private var lastShakeTimestamp: Long = 0L
    private var lastAimingTimestamp: Long = 0L
    private var lastCellChangeTimestamp: Long = 0L
    private var lastTargetCell: Int = -1

    // Filtro pasa-bajas para eliminar vibraciones y lecturas erráticas
    private var smoothedX: Float = 0f
    private var smoothedY: Float = 5.0f
    private val alpha: Float = 0.12f

    override fun isAvailable(): Boolean {
        return sensorManager != null && accelerometer != null
    }

    override fun startListening(listener: IGameSensorListener) {
        this.listener = listener
        lastTargetCell = -1
        sensorManager?.let { sm ->
            accelerometer?.let { acc ->
                sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
            }
            gyroscope?.let { gyro ->
                sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun stopListening() {
        sensorManager?.unregisterListener(this)
        this.listener = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || listener == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Detección de agitación (Shake) real e intencional:
                // Se eleva el umbral a 3.2 Gs (más de 31.4 m/s²) con enfriamiento de 1500 ms
                // para que movimientos ordinarios de la mano o inclinaciones no activen la recarga por error.
                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH
                val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                if (gForce > 3.2f && (now - lastShakeTimestamp > 1500L)) {
                    lastShakeTimestamp = now
                    listener?.onKineticShake()
                }

                // Inclinación mediante filtro pasa-bajas continuo (amortiguación inercial)
                smoothedX += alpha * (x - smoothedX)
                smoothedY += alpha * (y - smoothedY)

                if (now - lastAimingTimestamp > 100L) {
                    lastAimingTimestamp = now
                    processTilt(smoothedX, smoothedY, now)
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                // El giroscopio físico reporta velocidad angular (rad/s), no posición angular.
                // Se utiliza una ligera corrección inercial sin saltos abruptos.
                val now = System.currentTimeMillis()
                if (now - lastAimingTimestamp > 120L) {
                    val rollRate = event.values[1]
                    val pitchRate = event.values[0]
                    // Corrección sutil en caso de giros deliberados rápidos
                    if (kotlin.math.abs(rollRate) > 1.5f || kotlin.math.abs(pitchRate) > 1.5f) {
                        smoothedX += (rollRate * 0.2f)
                        smoothedY += (pitchRate * 0.2f)
                    }
                }
            }
        }
    }

    private fun processTilt(tiltX: Float, tiltY: Float, now: Long) {
        // Mapeo estable de inclinación a cuadrícula 8x8 con zona muerta central
        // Eje X (Columnas 0..7, A..H): Inclinación lateral (-3.5f a +3.5f)
        val col = ((tiltX + 3.5f) / 7.0f * 8).toInt().coerceIn(0, 7)
        // Eje Y (Filas 0..7, 1..8): Postura de juego natural con teléfono a ~45° (2.0f a 8.0f)
        val row = ((tiltY - 2.0f) / 6.0f * 8).toInt().coerceIn(0, 7)

        val targetCell = row * 8 + col

        // Histeresis y tiempo mínimo de transición entre casillas (180ms)
        // para evitar que la mira oscile erráticamente entre casillas adyacentes
        if (targetCell != lastTargetCell) {
            if (now - lastCellChangeTimestamp > 180L || lastTargetCell == -1) {
                lastTargetCell = targetCell
                lastCellChangeTimestamp = now
                listener?.onTiltAiming(targetCell, tiltY, tiltX)
            }
        } else {
            listener?.onTiltAiming(targetCell, tiltY, tiltX)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
