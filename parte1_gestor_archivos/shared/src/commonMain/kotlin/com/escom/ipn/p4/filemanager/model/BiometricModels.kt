package com.escom.ipn.p4.filemanager.model

enum class BiometricAuthResult {
    SUCCESS,
    FAILED,
    CANCELED,
    NOT_AVAILABLE,
    DEVICE_CREDENTIAL_SUCCESS
}

interface IBiometricAuthService {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(
        title: String = "Autenticación Requerida",
        subtitle: String = "Acción Sensible Protegida",
        description: String = "Verifique su identidad mediante biometría o credencial del dispositivo."
    ): BiometricAuthResult
}
