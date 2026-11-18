package com.escom.ipn.p4.filemanager.service

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.escom.ipn.p4.filemanager.model.BiometricAuthResult
import com.escom.ipn.p4.filemanager.model.IBiometricAuthService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidBiometricAuthService(
    private val context: Context,
    private val activityProvider: () -> FragmentActivity?
) : IBiometricAuthService {

    override fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(
        title: String,
        subtitle: String,
        description: String
    ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->
        val activity = activityProvider()
        if (activity == null) {
            continuation.resume(BiometricAuthResult.NOT_AVAILABLE)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (continuation.isActive) {
                    continuation.resume(BiometricAuthResult.SUCCESS)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (continuation.isActive) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            continuation.resume(BiometricAuthResult.CANCELED)
                        BiometricPrompt.ERROR_NO_BIOMETRICS, BiometricPrompt.ERROR_HW_NOT_PRESENT, BiometricPrompt.ERROR_HW_UNAVAILABLE ->
                            continuation.resume(BiometricAuthResult.NOT_AVAILABLE)
                        else ->
                            continuation.resume(BiometricAuthResult.FAILED)
                    }
                }
            }

            override fun onAuthenticationFailed() {
                // Intento fallido individual de lectura; el prompt permanece abierto en el sistema
            }
        })

        prompt.authenticate(promptInfo)

        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
        }
    }
}
