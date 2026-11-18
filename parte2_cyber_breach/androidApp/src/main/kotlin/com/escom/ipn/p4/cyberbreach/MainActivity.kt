package com.escom.ipn.p4.cyberbreach

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.escom.ipn.p4.cyberbreach.repository.AndroidGameSaveRepository
import com.escom.ipn.p4.cyberbreach.service.AndroidAudioService
import com.escom.ipn.p4.cyberbreach.service.AndroidBluetoothGameService
import com.escom.ipn.p4.cyberbreach.service.AndroidGameSensorManager
import com.escom.ipn.p4.cyberbreach.ui.CyberBreachScreen
import com.escom.ipn.p4.cyberbreach.ui.theme.CyberBreachTheme
import com.escom.ipn.p4.cyberbreach.viewmodel.CyberBreachViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: AndroidGameSensorManager
    private lateinit var bluetoothService: AndroidBluetoothGameService
    private lateinit var viewModel: CyberBreachViewModel

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (::bluetoothService.isInitialized) {
            bluetoothService.refreshPairedDevices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AndroidGameSaveRepository(applicationContext)
        val audioService = AndroidAudioService()
        sensorManager = AndroidGameSensorManager(applicationContext)
        bluetoothService = AndroidBluetoothGameService(applicationContext)
        viewModel = CyberBreachViewModel(
            saveRepository = repository,
            audioService = audioService,
            sensorManager = sensorManager,
            bluetoothService = bluetoothService
        )

        requestBluetoothPermissionsIfNeeded()

        setContent {
            val state by viewModel.uiState.collectAsState()

            CyberBreachTheme(cyberTheme = state.cyberTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CyberBreachScreen(
                        viewModel = viewModel,
                        onRequestBluetoothPermissions = { requestBluetoothPermissionsIfNeeded(forcePrompt = true) },
                        onOpenBluetoothSettings = {
                            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        }
                    )
                }
            }
        }
    }

    private fun requestBluetoothPermissionsIfNeeded(forcePrompt: Boolean = false) {
        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty() || forcePrompt) {
            val toRequest = if (missing.isNotEmpty()) missing.toTypedArray() else required
            bluetoothPermissionLauncher.launch(toRequest)
        } else if (::bluetoothService.isInitialized) {
            bluetoothService.refreshPairedDevices()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::viewModel.isInitialized) {
            viewModel.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bluetoothService.isInitialized) {
            bluetoothService.disconnect()
        }
    }
}
