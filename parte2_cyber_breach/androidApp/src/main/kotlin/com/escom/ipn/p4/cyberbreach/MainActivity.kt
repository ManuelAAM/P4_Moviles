package com.escom.ipn.p4.cyberbreach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

        setContent {
            val state by viewModel.uiState.collectAsState()

            CyberBreachTheme(cyberTheme = state.cyberTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CyberBreachScreen(viewModel = viewModel)
                }
            }
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
