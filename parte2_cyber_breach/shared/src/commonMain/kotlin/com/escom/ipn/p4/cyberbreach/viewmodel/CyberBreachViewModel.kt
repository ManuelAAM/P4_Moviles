package com.escom.ipn.p4.cyberbreach.viewmodel

import com.escom.ipn.p4.cyberbreach.model.CELL_HIDDEN
import com.escom.ipn.p4.cyberbreach.model.CyberBreachPacket
import com.escom.ipn.p4.cyberbreach.model.CyberTheme
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GamePacketType
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.IGameSensorListener
import com.escom.ipn.p4.cyberbreach.model.IGameSensorManager
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata
import com.escom.ipn.p4.cyberbreach.model.SkillType
import com.escom.ipn.p4.cyberbreach.repository.IGameSaveRepository
import com.escom.ipn.p4.cyberbreach.service.GameEngine
import com.escom.ipn.p4.cyberbreach.service.IAudioService
import com.escom.ipn.p4.cyberbreach.service.IBluetoothGameService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CyberScreen {
    TITLE_MENU,
    GAMEPLAY
}

data class CyberBreachUiState(
    val currentScreen: CyberScreen = CyberScreen.TITLE_MENU,
    val gameState: GameState = GameEngine.startNewGame(),
    val savedGames: List<SaveMetadata> = emptyList(),
    val preferredFormat: SaveFormat = SaveFormat.JSON,
    val cyberTheme: CyberTheme = CyberTheme.GUINDA_IPN,
    val isSoundEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val rawViewingContent: String? = null,
    val rawViewingTitle: String? = null,
    val rawViewingFormat: SaveFormat? = null,
    val activeFilterCategory: String = "Todas",
    // Práctica 6: Multijugador Bluetooth y Sensores
    val isBluetoothLobbyOpen: Boolean = false,
    val isHosting: Boolean = false,
    val bluetoothPairedDevices: List<Pair<String, String>> = emptyList(),
    val bluetoothStatusText: String = "Desconectado"
)

class CyberBreachViewModel(
    private val saveRepository: IGameSaveRepository,
    private val audioService: IAudioService,
    val sensorManager: IGameSensorManager? = null,
    val bluetoothService: IBluetoothGameService? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(CyberBreachUiState())
    val uiState: StateFlow<CyberBreachUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSavedGamesList()
        restoreActiveSessionIfAny()
        startTimerLoop()
        setupSensorListeners()
        setupBluetoothListeners()
    }

    private fun setupSensorListeners() {
        sensorManager?.startListening(object : IGameSensorListener {
            override fun onTiltAiming(targetCell: Int, pitch: Float, roll: Float) {
                val current = _uiState.value.gameState
                if (_uiState.value.currentScreen == CyberScreen.GAMEPLAY && !current.isGameOver) {
                    val nextState = GameEngine.updateSensorTarget(current, targetCell)
                    _uiState.update { it.copy(gameState = nextState) }
                }
            }

            override fun onKineticShake() {
                val current = _uiState.value.gameState
                if (_uiState.value.currentScreen == CyberScreen.GAMEPLAY && !current.isGameOver) {
                    val (nextState, feedback) = GameEngine.applyKineticShake(current)
                    if (nextState.playerEnergy > current.playerEnergy) {
                        audioService.playSkill()
                    }
                    _uiState.update { it.copy(gameState = nextState, statusMessage = feedback) }
                }
            }
        })
    }

    private fun setupBluetoothListeners() {
        val bt = bluetoothService ?: return
        coroutineScope.launch {
            bt.incomingPacket.collect { packet ->
                if (packet != null) {
                    handleIncomingPacket(packet)
                }
            }
        }
        coroutineScope.launch {
            bt.pairedDevices.collect { list ->
                _uiState.update { it.copy(bluetoothPairedDevices = list) }
            }
        }
        coroutineScope.launch {
            bt.connectionStatusMessage.collect { msg ->
                _uiState.update { it.copy(bluetoothStatusText = msg) }
            }
        }
        coroutineScope.launch {
            bt.isConnected.collect { connected ->
                if (connected) {
                    _uiState.update {
                        it.copy(
                            currentScreen = CyberScreen.GAMEPLAY,
                            isBluetoothLobbyOpen = false,
                            statusMessage = "¡Enlace Bluetooth P2P activo!"
                        )
                    }
                }
            }
        }
    }

    private fun handleIncomingPacket(packet: CyberBreachPacket) {
        val current = _uiState.value.gameState
        when (packet.type) {
            GamePacketType.HANDSHAKE -> {
                val oppName = packet.senderName.ifBlank { "Contrincante" }
                val nextState = current.copy(
                    opponentName = oppName,
                    isBluetoothSession = true,
                    bluetoothStatusMessage = if (current.isHost) "¡Tu turno de atacar!" else "Esperando jugada de $oppName..."
                )
                _uiState.update {
                    it.copy(
                        gameState = nextState,
                        currentScreen = CyberScreen.GAMEPLAY,
                        isBluetoothLobbyOpen = false,
                        statusMessage = "¡Conexión establecida con $oppName!"
                    )
                }
            }
            GamePacketType.ATTACK -> {
                val timestamp = "Turno ${current.turns + 1}"
                val (nextState, responsePacket) = GameEngine.handleIncomingRemoteAttack(
                    currentState = current,
                    incomingSeq = packet.seq,
                    targetCell = packet.targetCell,
                    skill = packet.skill,
                    senderName = packet.senderName,
                    timestamp = timestamp
                )
                if (nextState.isDefeat) {
                    audioService.playAlarm()
                } else if (responsePacket.hitResult != "MISS") {
                    audioService.playAlarm()
                } else {
                    audioService.playMiss()
                }
                bluetoothService?.sendPacket(responsePacket)
                _uiState.update {
                    it.copy(
                        gameState = nextState,
                        statusMessage = "Ataque de ${packet.senderName} recibido en casilla ${packet.targetCell}. ¡Tu turno!"
                    )
                }
            }
            GamePacketType.ATTACK_RESULT -> {
                val timestamp = "Turno ${current.turns + 1}"
                val (nextState, feedback) = GameEngine.applyRemoteAttackResult(
                    currentState = current,
                    resultPacket = packet,
                    timestamp = timestamp
                )
                if (nextState.isVictory) {
                    audioService.playVictory()
                } else if (packet.hitResult != "MISS") {
                    audioService.playHit()
                } else {
                    audioService.playMiss()
                }
                _uiState.update {
                    it.copy(
                        gameState = nextState,
                        statusMessage = feedback
                    )
                }
            }
            GamePacketType.DISCONNECT -> {
                _uiState.update {
                    it.copy(
                        statusMessage = "El oponente ${current.opponentName} se ha desconectado.",
                        gameState = it.gameState.copy(bluetoothStatusMessage = "Conexión cerrada por el oponente")
                    )
                }
            }
            else -> {}
        }
    }

    private fun restoreActiveSessionIfAny() {
        coroutineScope.launch {
            val savedSession = saveRepository.loadActiveSession()
            if (savedSession != null && !savedSession.isGameOver) {
                _uiState.update {
                    it.copy(
                        gameState = savedSession,
                        statusMessage = "Partida anterior disponible para reanudar."
                    )
                }
            }
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value.gameState
                if (_uiState.value.currentScreen == CyberScreen.GAMEPLAY && !current.isGameOver) {
                    val nextElapsed = current.elapsedSeconds + 1
                    val cooledState = if (current.kineticCooldownSeconds > 0) {
                        GameEngine.decrementKineticCooldown(current)
                    } else current
                    val updatedState = cooledState.copy(elapsedSeconds = nextElapsed)
                    _uiState.update { it.copy(gameState = updatedState) }

                    // Auto-guardado periódico (excepto en multijugador Bluetooth volátil)
                    if (nextElapsed % 5 == 0 && current.gameMode != GameMode.BLUETOOTH_P2P) {
                        saveRepository.saveActiveSession(updatedState)
                    }
                }
            }
        }
    }

    fun navigateToTitle() {
        audioService.playClick()
        _uiState.update { it.copy(currentScreen = CyberScreen.TITLE_MENU) }
    }

    fun navigateToGame() {
        audioService.playClick()
        _uiState.update { it.copy(currentScreen = CyberScreen.GAMEPLAY) }
    }

    fun startNewGame(mode: GameMode = GameMode.INFILTRATION) {
        val fresh = GameEngine.startNewGame(mode)
        _uiState.update {
            it.copy(
                gameState = fresh,
                currentScreen = CyberScreen.GAMEPLAY,
                statusMessage = when (mode) {
                    GameMode.INFILTRATION -> "Infiltración en solitario iniciada. Neutraliza la red enemiga."
                    GameMode.CPU_DUEL -> "Cyber Duelo 1v1 vs CPU iniciado. ¡Defiende tu infraestructura!"
                    GameMode.BLUETOOTH_P2P -> "Combate Bluetooth local 1v1."
                }
            )
        }
        audioService.playClick()
        coroutineScope.launch {
            saveRepository.saveActiveSession(fresh)
        }
    }

    fun returnToMainMenu() {
        audioService.playClick()
        coroutineScope.launch {
            if (_uiState.value.gameState.gameMode != GameMode.BLUETOOTH_P2P) {
                saveRepository.saveActiveSession(_uiState.value.gameState)
            }
            loadSavedGamesList()
        }
        _uiState.update { it.copy(currentScreen = CyberScreen.TITLE_MENU) }
    }

    fun onCellClicked(cellIndex: Int) {
        val current = _uiState.value.gameState
        if (current.isGameOver) return

        // MODO BLUETOOTH P2P
        if (current.gameMode == GameMode.BLUETOOTH_P2P) {
            if (!current.isMyTurn) {
                _uiState.update { it.copy(errorMessage = "Espera tu turno. El contrincante está atacando.") }
                return
            }
            if (current.grid[cellIndex] != CELL_HIDDEN) {
                _uiState.update { it.copy(errorMessage = "Casilla ya atacada previamente.") }
                return
            }

            val attackSeq = current.turns + 1
            val packet = CyberBreachPacket(
                type = GamePacketType.ATTACK,
                seq = attackSeq,
                senderName = "Yo",
                targetCell = cellIndex,
                skill = current.activeSkill,
                timestamp = System.currentTimeMillis()
            )

            val sent = bluetoothService?.sendPacket(packet) ?: false
            if (sent) {
                audioService.playClick()
                val nextState = current.copy(
                    isMyTurn = false,
                    bluetoothStatusMessage = "Ataque enviado a casilla $cellIndex. Esperando confirmación..."
                )
                _uiState.update {
                    it.copy(
                        gameState = nextState,
                        statusMessage = "Ataque transmitido por Bluetooth. Esperando respuesta..."
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Error de comunicación Bluetooth con el oponente.") }
            }
            return
        }

        // MODO SOLITARIO O CPU DUEL
        val timestamp = "Turno ${current.turns + 1}"
        val (nextState, feedback) = GameEngine.executeTurn(current, cellIndex, timestamp)

        if (nextState.isVictory) {
            audioService.playVictory()
        } else if (nextState.isDefeat) {
            audioService.playAlarm()
        } else if (nextState.score > current.score) {
            audioService.playHit()
        } else if (current.activeSkill != SkillType.NORMAL) {
            audioService.playSkill()
        } else if (nextState.playerNodes.any { pNode ->
                val prevNode = current.playerNodes.find { it.id == pNode.id }
                prevNode != null && pNode.hits > prevNode.hits
            }) {
            audioService.playAlarm()
        } else {
            audioService.playMiss()
        }

        _uiState.update {
            it.copy(
                gameState = nextState,
                statusMessage = feedback
            )
        }

        coroutineScope.launch {
            saveRepository.saveActiveSession(nextState)
        }
    }

    fun fireAtSensorTarget() {
        val target = _uiState.value.gameState.targetedCellBySensor
        if (target != null) {
            onCellClicked(target)
        } else {
            _uiState.update { it.copy(errorMessage = "Inclina el dispositivo para apuntar con el giroscopio.") }
        }
    }

    fun triggerManualKineticShake() {
        val current = _uiState.value.gameState
        if (!current.isGameOver) {
            val (nextState, feedback) = GameEngine.applyKineticShake(current)
            if (nextState.playerEnergy > current.playerEnergy) {
                audioService.playSkill()
            }
            _uiState.update { it.copy(gameState = nextState, statusMessage = feedback) }
        }
    }

    fun selectSkill(skill: SkillType) {
        val currentEnergy = _uiState.value.gameState.playerEnergy
        if (skill.energyCost > currentEnergy) {
            _uiState.update {
                it.copy(errorMessage = "Energía insuficiente. Necesitas ${skill.energyCost} EN.")
            }
            return
        }

        audioService.playClick()
        _uiState.update {
            it.copy(
                gameState = it.gameState.copy(activeSkill = skill),
                statusMessage = "Habilidad '${skill.displayName}' seleccionada. Toque una casilla para activarla."
            )
        }
    }

    // --- MÉTODOS DE BLUETOOTH LOBBY ---

    fun openBluetoothLobby() {
        bluetoothService?.refreshPairedDevices()
        _uiState.update { it.copy(isBluetoothLobbyOpen = true) }
    }

    fun closeBluetoothLobby() {
        _uiState.update { it.copy(isBluetoothLobbyOpen = false) }
    }

    fun hostBluetoothGame() {
        val fresh = GameEngine.startNewGame(
            mode = GameMode.BLUETOOTH_P2P,
            isHost = true,
            opponentName = "Esperando jugador..."
        )
        _uiState.update { it.copy(gameState = fresh, isHosting = true) }
        bluetoothService?.startHosting("Jugador 1 (Host)")
    }

    fun connectToBluetoothHost(deviceAddress: String, hostName: String) {
        val fresh = GameEngine.startNewGame(
            mode = GameMode.BLUETOOTH_P2P,
            isHost = false,
            opponentName = hostName
        )
        _uiState.update { it.copy(gameState = fresh, isHosting = false) }
        bluetoothService?.connectToHost(deviceAddress, "Jugador 2 (Cliente)")
    }

    fun disconnectBluetooth() {
        bluetoothService?.disconnect()
        _uiState.update { it.copy(isHosting = false) }
    }

    fun onPause() {
        sensorManager?.stopListening()
    }

    fun onResume() {
        setupSensorListeners()
    }

    // --- GUARDADO Y PERSISTENCIA ---

    fun saveGame(title: String, categoryTag: String, format: SaveFormat = _uiState.value.preferredFormat) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value.gameState
            val result = saveRepository.saveGame(current, format, title, categoryTag)
            result.fold(
                onSuccess = { meta ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "Partida guardada exitosamente en formato ${format.name} (${meta.fileName})."
                        )
                    }
                    loadSavedGamesList()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al guardar partida: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    fun loadGame(saveId: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = saveRepository.loadGame(saveId)
            result.fold(
                onSuccess = { (loadedState, meta) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            gameState = loadedState,
                            currentScreen = CyberScreen.GAMEPLAY,
                            statusMessage = "Partida '${meta.title}' cargada correctamente desde ${meta.format.name}."
                        )
                    }
                    audioService.playClick()
                    saveRepository.saveActiveSession(loadedState)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo cargar la partida: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    fun deleteSave(saveId: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = saveRepository.deleteSave(saveId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Partida eliminada.") }
                    loadSavedGamesList()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${err.message}") }
                }
            )
        }
    }

    fun updateSaveCategory(saveId: String, newTag: String) {
        coroutineScope.launch {
            val result = saveRepository.updateCategoryTag(saveId, newTag)
            if (result.isSuccess) {
                _uiState.update { it.copy(statusMessage = "Categoría actualizada a '$newTag'.") }
                loadSavedGamesList()
            }
        }
    }

    fun viewRawSaveContent(saveId: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val save = _uiState.value.savedGames.find { it.id == saveId }
            val result = saveRepository.readSaveRawContent(saveId)
            result.fold(
                onSuccess = { raw ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rawViewingContent = raw,
                            rawViewingTitle = save?.title ?: "Archivo",
                            rawViewingFormat = save?.format ?: SaveFormat.JSON
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Error al leer archivo: ${err.message}")
                    }
                }
            )
        }
    }

    fun closeRawViewer() {
        _uiState.update { it.copy(rawViewingContent = null, rawViewingTitle = null, rawViewingFormat = null) }
    }

    fun loadSavedGamesList() {
        coroutineScope.launch {
            val saves = saveRepository.listSaves()
            _uiState.update { it.copy(savedGames = saves) }
        }
    }

    fun setPreferredFormat(format: SaveFormat) {
        _uiState.update { it.copy(preferredFormat = format, statusMessage = "Formato de guardado predeterminado: ${format.name}") }
    }

    fun setCyberTheme(theme: CyberTheme) {
        _uiState.update { it.copy(cyberTheme = theme) }
    }

    fun toggleSound() {
        val next = !_uiState.value.isSoundEnabled
        audioService.isMuted = !next
        _uiState.update { it.copy(isSoundEnabled = next, statusMessage = if (next) "Sonido activado" else "Sonido silenciado") }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(activeFilterCategory = category) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
