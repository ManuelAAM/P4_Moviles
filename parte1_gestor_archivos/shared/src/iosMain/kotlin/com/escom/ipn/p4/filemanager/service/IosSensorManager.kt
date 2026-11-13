package com.escom.ipn.p4.filemanager.service

import com.escom.ipn.p4.filemanager.model.ISensorManager
import com.escom.ipn.p4.filemanager.model.MotionTiltState
import com.escom.ipn.p4.filemanager.model.SensorData
import com.escom.ipn.p4.filemanager.model.SensorStatus
import com.escom.ipn.p4.filemanager.model.SensorType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosSensorManager : ISensorManager {
    private val _accelerometerFlow = MutableStateFlow(SensorData(SensorType.ACCELEROMETER, status = SensorStatus.ACTIVE))
    override val accelerometerFlow: StateFlow<SensorData> = _accelerometerFlow.asStateFlow()

    private val _gyroscopeFlow = MutableStateFlow(SensorData(SensorType.GYROSCOPE, status = SensorStatus.ACTIVE))
    override val gyroscopeFlow: StateFlow<SensorData> = _gyroscopeFlow.asStateFlow()

    private val _shakeEventFlow = MutableStateFlow(0L)
    override val shakeEventFlow: StateFlow<Long> = _shakeEventFlow.asStateFlow()

    private val _tiltStateFlow = MutableStateFlow(MotionTiltState())
    override val tiltStateFlow: StateFlow<MotionTiltState> = _tiltStateFlow.asStateFlow()

    override fun isSensorAvailable(type: SensorType): Boolean = true
    override fun startListening(samplingPeriodMs: Long) {}
    override fun stopListening() {}
    override fun setSimulationMode(enabled: Boolean) {}
    override fun injectSimulatedReading(type: SensorType, x: Float, y: Float, z: Float) {
        val data = SensorData(type = type, x = x, y = y, z = z, isSimulated = true)
        if (type == SensorType.ACCELEROMETER) _accelerometerFlow.value = data
        else _gyroscopeFlow.value = data
    }
}
