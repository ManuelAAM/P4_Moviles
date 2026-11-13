package com.escom.ipn.p4.filemanager.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.geometry.Offset
import com.escom.ipn.p4.filemanager.ui.components.BiometricVaultDialog
import com.escom.ipn.p4.filemanager.ui.components.BluetoothTransferDialog
import com.escom.ipn.p4.filemanager.ui.components.SensorTelemetryDialog
import com.escom.ipn.p4.filemanager.ui.components.WebProxyDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.escom.ipn.p4.filemanager.model.AppColorTheme
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.ui.components.BreadcrumbsBar
import com.escom.ipn.p4.filemanager.ui.components.CreateFolderDialog
import com.escom.ipn.p4.filemanager.ui.components.CreateTextFileDialog
import com.escom.ipn.p4.filemanager.ui.components.DeleteConfirmDialog
import com.escom.ipn.p4.filemanager.ui.components.FileDetailsDialog
import com.escom.ipn.p4.filemanager.ui.components.FileItemRow
import com.escom.ipn.p4.filemanager.ui.components.FileViewerSheet
import com.escom.ipn.p4.filemanager.ui.components.RenameDialog
import com.escom.ipn.p4.filemanager.viewmodel.FileManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados de diálogos
    var isSearchActive by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    var itemForDetails by remember { mutableStateOf<FileItem?>(null) }
    var itemForRename by remember { mutableStateOf<FileItem?>(null) }
    var itemForDelete by remember { mutableStateOf<FileItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showBiometricVaultDialog by remember { mutableStateOf(false) }

    // Notificaciones Snackbar
    LaunchedEffect(state.statusMessage, state.errorMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Buscar archivos...") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Cerrar búsqueda")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text(
                                text = "Gestor de Archivos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.colorTheme == AppColorTheme.GUINDA_IPN) "Tema: Guinda IPN" else "Tema: Azul ESCOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (state.breadcrumbs.size > 1 && !isSearchActive) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Subir de nivel")
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }

                    // Ejercicio 1: Telemetría de Sensores
                    IconButton(onClick = { viewModel.openSensorTelemetryDialog() }) {
                        Icon(Icons.Default.Sensors, contentDescription = "Sensores e Interacción")
                    }

                    // Ejercicio 2: Transferencia Bluetooth
                    IconButton(onClick = { viewModel.openBluetoothTransferDialog() }) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Transferencia Bluetooth")
                    }

                    // Ejercicio 3: Consulta Web Proxy por Bluetooth
                    IconButton(onClick = { viewModel.openWebProxyDialog() }) {
                        Icon(Icons.Default.Language, contentDescription = "Consulta Web Proxy")
                    }

                    // Bóveda Biométrica
                    IconButton(onClick = { showBiometricVaultDialog = true }) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Bóveda Protegida")
                    }

                    // Selector de tema institucional
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Cambiar tema")
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Guinda IPN") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = Color(0xFF6C1D45)
                                    )
                                },
                                onClick = {
                                    viewModel.setColorTheme(AppColorTheme.GUINDA_IPN)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Azul ESCOM") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = Color(0xFF003366)
                                    )
                                },
                                onClick = {
                                    viewModel.setColorTheme(AppColorTheme.AZUL_ESCOM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            showCreateFolderDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nueva Carpeta")
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            showCreateFileDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo Archivo TXT")
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Clear else Icons.Default.Add,
                        contentDescription = "Acciones"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barra de filtros rápidos
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.isFavoritesOnly,
                        onClick = { viewModel.toggleFavoritesFilter() },
                        label = { Text("Favoritos") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (state.isFavoritesOnly) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                    FilterChip(
                        selected = state.isRecentsOnly,
                        onClick = { viewModel.toggleRecentsFilter() },
                        label = { Text("Recientes") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Breadcrumbs de navegación
            BreadcrumbsBar(
                breadcrumbs = state.breadcrumbs,
                onBreadcrumbClick = { viewModel.navigateBreadcrumb(it) }
            )

            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.filteredItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.isFavoritesOnly) "No hay archivos en favoritos"
                            else if (state.isRecentsOnly) "No hay archivos recientes"
                            else if (state.searchQuery.isNotBlank()) "Sin coincidencias para '${state.searchQuery}'"
                            else "Esta carpeta está vacía",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.filteredItems, key = { it.path }) { item ->
                            FileItemRow(
                                item = item,
                                onClick = { viewModel.navigateTo(item) },
                                onToggleFavorite = { viewModel.toggleFavorite(item) },
                                onRename = { itemForRename = item },
                                onDelete = { itemForDelete = item },
                                onCopy = {
                                    // Copiar a la misma carpeta o raíz
                                    viewModel.copyItem(item, state.currentPath)
                                },
                                onMove = {
                                    viewModel.moveItem(item, state.currentPath)
                                },
                                onDetails = { itemForDetails = item },
                                onBluetoothSend = { viewModel.openBluetoothTransferDialog(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogos modales
    itemForDetails?.let { item ->
        FileDetailsDialog(item = item, onDismiss = { itemForDetails = null })
    }

    itemForRename?.let { item ->
        RenameDialog(
            item = item,
            onDismiss = { itemForRename = null },
            onConfirm = { newName ->
                viewModel.renameItem(item, newName)
                itemForRename = null
            }
        )
    }

    itemForDelete?.let { item ->
        DeleteConfirmDialog(
            item = item,
            onDismiss = { itemForDelete = null },
            onConfirm = {
                viewModel.deleteItem(item)
                itemForDelete = null
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { folderName ->
                viewModel.createFolder(folderName)
                showCreateFolderDialog = false
            }
        )
    }

    if (showCreateFileDialog) {
        CreateTextFileDialog(
            onDismiss = { showCreateFileDialog = false },
            onConfirm = { name, content ->
                viewModel.createTextFile(name, content)
                showCreateFileDialog = false
            }
        )
    }

    // Visor de archivos (Sheet)
    state.selectedFileForViewing?.let { file ->
        FileViewerSheet(
            item = file,
            content = state.viewingContent,
            tiltPanOffset = Offset(state.tiltState.panOffsetX, state.tiltState.panOffsetY),
            tiltPitch = state.tiltState.pitch,
            tiltRoll = state.tiltState.roll,
            onDismiss = { viewModel.closeFileViewer() }
        )
    }

    // Diálogos de Práctica 6 (Sensores, Bluetooth, Proxy Web, Biometría)
    if (state.showSensorTelemetryDialog) {
        SensorTelemetryDialog(
            accelData = state.sensorDataAccel,
            gyroData = state.sensorDataGyro,
            tiltState = state.tiltState,
            isSimulationMode = state.isSensorSimulationEnabled,
            onToggleSimulation = { viewModel.toggleSensorSimulation() },
            onInjectSimulated = { type, x, y, z -> viewModel.injectSimulatedSensorReading(type, x, y, z) },
            onDismiss = { viewModel.closeSensorTelemetryDialog() }
        )
    }

    if (state.showBluetoothTransferDialog) {
        BluetoothTransferDialog(
            selectedFile = state.selectedFileForTransfer,
            availableFiles = state.items,
            devices = state.bluetoothDevices,
            connectedPeer = state.connectedPeer,
            activeTransfer = state.activeTransfer,
            transferHistory = state.transferHistory,
            pendingOffer = state.pendingIncomingOffer,
            onConnectDevice = { viewModel.connectToBluetoothDevice(it) },
            onSendFile = { file, dev -> viewModel.sendFileViaBluetooth(file, dev) },
            onSimulateTransfer = { file -> viewModel.simulateBluetoothTransfer(file) },
            onShareViaSystemBluetooth = { file -> shareFileViaSystemBluetooth(context, file) },
            onSimulateIncomingOffer = { viewModel.simulateIncomingTransferOffer() },
            onAcceptIncoming = { viewModel.acceptIncomingTransfer() },
            onRejectIncoming = { viewModel.rejectIncomingTransfer() },
            onCancelTransfer = { viewModel.cancelActiveTransfer() },
            onDismiss = { viewModel.closeBluetoothTransferDialog() }
        )
    }

    if (state.showWebProxyDialog) {
        WebProxyDialog(
            initialUrl = state.proxyUrlInput,
            lastResponse = state.lastProxyResponse,
            cacheRecords = state.webCacheRecords,
            isLoading = state.isProxyLoading,
            onExecuteRequest = { viewModel.executeWebProxyRequest(it) },
            onClearCache = { viewModel.clearWebProxyCache() },
            onDismiss = { viewModel.closeWebProxyDialog() }
        )
    }

    if (showBiometricVaultDialog) {
        BiometricVaultDialog(
            isUnlocked = state.isVaultUnlocked,
            onUnlock = { viewModel.unlockVaultWithBiometrics() },
            onLock = { viewModel.lockVault() },
            onDismiss = { showBiometricVaultDialog = false }
        )
    }
}

private fun shareFileViaSystemBluetooth(context: Context, item: FileItem) {
    try {
        val rootDir = context.filesDir
        val file = if (File(item.path).isAbsolute) File(item.path) else File(rootDir, item.path)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no se encuentra disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val btIntent = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.android.bluetooth")
        }
        try {
            context.startActivity(btIntent)
        } catch (_: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Transferir archivo por Bluetooth")
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir por Bluetooth: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
