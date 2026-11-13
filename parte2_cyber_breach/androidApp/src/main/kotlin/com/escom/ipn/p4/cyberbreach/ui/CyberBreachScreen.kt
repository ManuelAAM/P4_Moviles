package com.escom.ipn.p4.cyberbreach.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.CyberTheme
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.ui.components.BluetoothLobbyDialog
import com.escom.ipn.p4.cyberbreach.ui.components.CyberGrid
import com.escom.ipn.p4.cyberbreach.ui.components.CyberStatusBar
import com.escom.ipn.p4.cyberbreach.ui.components.GameOverDialog
import com.escom.ipn.p4.cyberbreach.ui.components.PlayerDefenseGrid
import com.escom.ipn.p4.cyberbreach.ui.components.RawFileViewerDialog
import com.escom.ipn.p4.cyberbreach.ui.components.SaveGameDialog
import com.escom.ipn.p4.cyberbreach.ui.components.SaveManagerDialog
import com.escom.ipn.p4.cyberbreach.ui.components.SettingsDialog
import com.escom.ipn.p4.cyberbreach.ui.components.SkillsPanel
import com.escom.ipn.p4.cyberbreach.ui.screens.TitleMenuScreen
import com.escom.ipn.p4.cyberbreach.viewmodel.CyberBreachViewModel
import com.escom.ipn.p4.cyberbreach.viewmodel.CyberScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberBreachScreen(
    viewModel: CyberBreachViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavesManager by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

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

    // Si la pantalla actual es el Menú Inicial con estilo de videojuego
    if (state.currentScreen == CyberScreen.TITLE_MENU) {
        Box(modifier = Modifier.fillMaxSize()) {
            TitleMenuScreen(
                savedGames = state.savedGames,
                cyberTheme = state.cyberTheme,
                hasActiveSession = state.gameState.turns > 0 && !state.gameState.isGameOver,
                onStartGame = { mode -> viewModel.startNewGame(mode) },
                onResumeGame = { viewModel.navigateToGame() },
                onOpenSavesManager = {
                    viewModel.loadSavedGamesList()
                    showSavesManager = true
                },
                onOpenSettings = { showSettings = true },
                onOpenBluetoothLobby = { viewModel.openBluetoothLobby() }
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } else {
        // Pantalla de Juego Táctico
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Cyber Breach (Net Strike)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.cyberTheme == CyberTheme.GUINDA_IPN) "Tema: Guinda IPN" else "Tema: Azul ESCOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.returnToMainMenu() }) {
                            Icon(Icons.Default.Home, contentDescription = "Menú Principal")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar partida")
                        }
                        IconButton(onClick = {
                            viewModel.loadSavedGamesList()
                            showSavesManager = true
                        }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Gestor de partidas")
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                        IconButton(onClick = { viewModel.startNewGame(state.gameState.gameMode) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reiniciar partida")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de estado táctico
                CyberStatusBar(gameState = state.gameState)

                // Banner Multijugador Bluetooth (Práctica 6)
                if (state.gameState.gameMode == GameMode.BLUETOOTH_P2P) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.gameState.isMyTurn) Color(0xFF064E3B) else Color(0xFF78350F)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.gameState.isMyTurn) Icons.Default.Bolt else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = if (state.gameState.isMyTurn) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (state.gameState.isMyTurn) "🟢 ¡TU TURNO DE ATACAR!" else "⏳ TURNO DEL CONTRINCANTE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.gameState.isMyTurn) Color(0xFF34D399) else Color(0xFFFBBF24)
                                )
                                Text(
                                    text = state.gameState.bluetoothStatusMessage ?: "Oponente: ${state.gameState.opponentName}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // En modo CPU_DUEL o BLUETOOTH_P2P, mostrar selector de vista (Ataque vs Defensa)
                if (state.gameState.gameMode == GameMode.CPU_DUEL || state.gameState.gameMode == GameMode.BLUETOOTH_P2P) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("RED ENEMIGA (ATACAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("TU RED (DEFENSA)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                if ((state.gameState.gameMode != GameMode.CPU_DUEL && state.gameState.gameMode != GameMode.BLUETOOTH_P2P) || selectedTab == 0) {
                    // Panel de selección de habilidades especiales
                    SkillsPanel(
                        currentEnergy = state.gameState.playerEnergy,
                        activeSkill = state.gameState.activeSkill,
                        onSkillSelected = { viewModel.selectSkill(it) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Cuadrícula Cyber Grid 8x8 (Ataque a la red enemiga)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        CyberGrid(
                            grid = state.gameState.grid,
                            gridSize = state.gameState.gridSize,
                            targetedCell = state.gameState.targetedCellBySensor,
                            onCellClick = { cellIndex -> viewModel.onCellClicked(cellIndex) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tarjeta de Sensores Tácticos: Giroscopio y Recarga Cinética
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Mira del Giroscopio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Adjust,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val targetCell = state.gameState.targetedCellBySensor
                                    val coordText = if (targetCell != null) {
                                        val r = targetCell / 8 + 1
                                        val c = ('A'.code + (targetCell % 8)).toChar()
                                        "$c$r (Fila $r, Col $c)"
                                    } else "Inclinando..."
                                    Text(
                                        text = "Giroscopio: $coordText",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.fireAtSensorTarget() },
                                    enabled = state.gameState.targetedCellBySensor != null &&
                                            (!state.gameState.isBluetoothSession || state.gameState.isMyTurn) &&
                                            !state.gameState.isGameOver,
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Disparar Mira", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Recarga Cinética por Agitación
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = if (state.gameState.kineticCooldownSeconds == 0) Color(0xFF34D399) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val shakeStatus = if (state.gameState.kineticCooldownSeconds == 0) {
                                        "Recarga Cinética: LISTA (+5 EN)"
                                    } else {
                                        "Enfriamiento cinético: ${state.gameState.kineticCooldownSeconds}s"
                                    }
                                    Text(
                                        text = shakeStatus,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (state.gameState.kineticCooldownSeconds == 0) Color(0xFF34D399) else Color(0xFFFBBF24)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.triggerManualKineticShake() },
                                    enabled = state.gameState.kineticCooldownSeconds == 0 && !state.gameState.isGameOver,
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Agitar (Test)", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Vista de defensa en Cyber Duelo / Multijugador: Cuadrícula con los nodos propios
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        PlayerDefenseGrid(
                            playerGrid = state.gameState.playerGrid,
                            playerNodes = state.gameState.playerNodes,
                            gridSize = state.gameState.gridSize
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Consola / Terminal de feedback de acciones
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val lastAction = state.gameState.history.lastOrNull()
                        Text(
                            text = if (lastAction != null) {
                                "> [${lastAction.actionType}] ${lastAction.result}"
                            } else {
                                "> SISTEMA LISTO. SELECCIONE UN OBJETIVO EN LA RED."
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Diálogos globales (accesibles desde menú o gameplay)
    if (showSaveDialog) {
        SaveGameDialog(
            initialFormat = state.preferredFormat,
            onDismiss = { showSaveDialog = false },
            onSave = { title, tag, format ->
                viewModel.saveGame(title, tag, format)
                showSaveDialog = false
            }
        )
    }

    if (showSavesManager) {
        SaveManagerDialog(
            saves = state.savedGames,
            onDismiss = { showSavesManager = false },
            onLoad = { saveId ->
                viewModel.loadGame(saveId)
                showSavesManager = false
            },
            onDelete = { saveId -> viewModel.deleteSave(saveId) },
            onViewRaw = { saveId -> viewModel.viewRawSaveContent(saveId) },
            onUpdateTag = { saveId, newTag -> viewModel.updateSaveCategory(saveId, newTag) }
        )
    }

    if (showSettings) {
        SettingsDialog(
            preferredFormat = state.preferredFormat,
            currentTheme = state.cyberTheme,
            isSoundEnabled = state.isSoundEnabled,
            onFormatChange = { viewModel.setPreferredFormat(it) },
            onThemeChange = { viewModel.setCyberTheme(it) },
            onToggleSound = { viewModel.toggleSound() },
            onModeChange = { mode ->
                viewModel.startNewGame(mode)
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }

    // Visor de código crudo (.txt, .xml, .json)
    state.rawViewingContent?.let { content ->
        RawFileViewerDialog(
            title = state.rawViewingTitle ?: "Archivo",
            rawContent = content,
            format = state.rawViewingFormat ?: state.preferredFormat,
            onDismiss = { viewModel.closeRawViewer() }
        )
    }

    // Lobby Multijugador Bluetooth
    if (state.isBluetoothLobbyOpen) {
        BluetoothLobbyDialog(
            isHosting = state.isHosting,
            statusText = state.bluetoothStatusText,
            pairedDevices = state.bluetoothPairedDevices,
            onDismiss = { viewModel.closeBluetoothLobby() },
            onHostGame = { viewModel.hostBluetoothGame() },
            onConnectDevice = { address, name -> viewModel.connectToBluetoothHost(address, name) },
            onRefreshDevices = { viewModel.bluetoothService?.refreshPairedDevices() }
        )
    }

    // Fin de partida (Victoria o Derrota)
    if (state.gameState.isGameOver) {
        GameOverDialog(
            gameState = state.gameState,
            onRestart = { viewModel.startNewGame(state.gameState.gameMode) },
            onSaveGame = { showSaveDialog = true },
            onMainMenu = { viewModel.returnToMainMenu() }
        )
    }
}
