package com.escom.ipn.p4.cyberbreach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.escom.ipn.p4.cyberbreach.repository.AndroidGameSaveRepository
import com.escom.ipn.p4.cyberbreach.service.AndroidAudioService
import com.escom.ipn.p4.cyberbreach.ui.CyberBreachScreen
import com.escom.ipn.p4.cyberbreach.ui.theme.CyberBreachTheme
import com.escom.ipn.p4.cyberbreach.viewmodel.CyberBreachViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = remember { AndroidGameSaveRepository(applicationContext) }
            val audioService = remember { AndroidAudioService() }
            val viewModel = remember { CyberBreachViewModel(repository, audioService) }
            val state by viewModel.uiState.collectAsState()

            CyberBreachTheme(cyberTheme = state.cyberTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CyberBreachScreen(viewModel = viewModel)
                }
            }
        }
    }
}
