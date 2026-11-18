package com.escom.ipn.p4.cyberbreach.service

import com.escom.ipn.p4.cyberbreach.model.IGameSensorListener
import com.escom.ipn.p4.cyberbreach.model.IGameSensorManager

class IosGameSensorManager : IGameSensorManager {
    override fun startListening(listener: IGameSensorListener) {
        // CoreMotion implementation stub for iOS
    }

    override fun stopListening() {
        // Stop CoreMotion updates
    }

    override fun isAvailable(): Boolean = true
}
