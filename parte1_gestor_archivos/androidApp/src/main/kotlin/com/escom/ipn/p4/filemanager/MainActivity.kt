package com.escom.ipn.p4.filemanager

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sm = AndroidSensorManager(applicationContext)
        sensorManager = sm
        val bio = AndroidBiometricAuthService(applicationContext) { this }
        val bt = AndroidBluetoothService(applicationContext)

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

    override fun onResume() {
        super.onResume()
        sensorManager?.startListening(60L)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.stopListening()
    }
}
