package com.escom.ipn.p4.filemanager.service

import com.escom.ipn.p4.filemanager.model.BiometricAuthResult
import com.escom.ipn.p4.filemanager.model.IBiometricAuthService

class IosBiometricAuthService : IBiometricAuthService {
    override fun isBiometricAvailable(): Boolean = true
    override suspend fun authenticate(title: String, subtitle: String, description: String): BiometricAuthResult {
        // En entorno simulado iOS retorna éxito
        return BiometricAuthResult.SUCCESS
    }
}
