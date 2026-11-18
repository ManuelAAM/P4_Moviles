package com.escom.ipn.p4.filemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.escom.ipn.p4.filemanager.repository.AndroidFileManagerRepository
import com.escom.ipn.p4.filemanager.service.AndroidBiometricAuthService
import com.escom.ipn.p4.filemanager.service.AndroidBluetoothService
import com.escom.ipn.p4.filemanager.service.AndroidSensorManager
import com.escom.ipn.p4.filemanager.ui.FileManagerScreen
import com.escom.ipn.p4.filemanager.ui.theme.FileManagerTheme
import com.escom.ipn.p4.filemanager.viewmodel.FileManagerViewModel

class MainActivity : FragmentActivity() {
    private var sensorManager: AndroidSensorManager? = null
    private var bluetoothService: AndroidBluetoothService? = null

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        bluetoothService?.refreshDevices()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sm = AndroidSensorManager(applicationContext)
        sensorManager = sm
        val bio = AndroidBiometricAuthService(applicationContext) { this }
        val bt = AndroidBluetoothService(applicationContext)
        bluetoothService = bt

        requestBluetoothPermissionsIfNeeded()

        setContent {
            val repository = remember { AndroidFileManagerRepository(applicationContext) }
            val viewModel = remember { FileManagerViewModel(repository, sm, bio, bt) }
            val state by viewModel.uiState.collectAsState()

            // Manejo de navegación atrás hacia la carpeta padre
            BackHandler(enabled = state.breadcrumbs.size > 1) {
                viewModel.navigateUp()
            }

            FileManagerTheme(colorTheme = state.colorTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FileManagerScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestBluetoothPermissionsIfNeeded() {
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

        if (missing.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.startListening(60L)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.stopListening()
    }
}
