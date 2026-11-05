package com.escom.ipn.p4.filemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.escom.ipn.p4.filemanager.repository.AndroidFileManagerRepository
import com.escom.ipn.p4.filemanager.ui.FileManagerScreen
import com.escom.ipn.p4.filemanager.ui.theme.FileManagerTheme
import com.escom.ipn.p4.filemanager.viewmodel.FileManagerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = remember { AndroidFileManagerRepository(applicationContext) }
            val viewModel = remember { FileManagerViewModel(repository) }
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
}
