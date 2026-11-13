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

    override fun isAvailable(): Boolean {
        return sensorManager != null && accelerometer != null
    }

    override fun startListening(listener: IGameSensorListener) {
        this.listener = listener
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

                // Detección de agitación (Shake) para recarga cinética
                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH
                val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                if (gForce > 2.2f && (now - lastShakeTimestamp > 1000L)) {
                    lastShakeTimestamp = now
                    listener?.onKineticShake()
                }

                // Si no hay giroscopio físico disponible, usamos el acelerómetro para inclinación (pitch / roll)
                if (gyroscope == null && (now - lastAimingTimestamp > 80L)) {
                    lastAimingTimestamp = now
                    processTilt(x, y)
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val now = System.currentTimeMillis()
                if (now - lastAimingTimestamp > 80L) {
                    lastAimingTimestamp = now
                    val rollRate = event.values[1]
                    val pitchRate = event.values[0]
                    processTilt(-rollRate * 5f, pitchRate * 5f)
                }
            }
        }
    }

    private fun processTilt(tiltX: Float, tiltY: Float) {
        // Mapeo de inclinación (-5.0f .. +5.0f) a cuadrícula 8x8
        // Columna (X): -3.5f a +3.5f -> 0..7
        val col = ((tiltX + 3.5f) / 7.0f * 8).toInt().coerceIn(0, 7)
        // Fila (Y): 2.0f a 8.0f -> 0..7 (postura de juego común con teléfono inclinado hacia el jugador)
        val row = ((tiltY - 2.0f) / 6.0f * 8).toInt().coerceIn(0, 7)

        val targetCell = row * 8 + col
        listener?.onTiltAiming(targetCell, tiltY, tiltX)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
