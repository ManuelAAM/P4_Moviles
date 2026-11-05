package com.escom.ipn.p4.cyberbreach.viewmodel

import com.escom.ipn.p4.cyberbreach.model.CyberTheme
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata
import com.escom.ipn.p4.cyberbreach.model.SkillType
import com.escom.ipn.p4.cyberbreach.repository.IGameSaveRepository
import com.escom.ipn.p4.cyberbreach.service.GameEngine
import com.escom.ipn.p4.cyberbreach.service.IAudioService
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
    val activeFilterCategory: String = "Todas"
)

class CyberBreachViewModel(
    private val saveRepository: IGameSaveRepository,
    private val audioService: IAudioService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(CyberBreachUiState())
    val uiState: StateFlow<CyberBreachUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSavedGamesList()
        restoreActiveSessionIfAny()
        startTimerLoop()
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
                    val updatedState = current.copy(elapsedSeconds = nextElapsed)
                    _uiState.update { it.copy(gameState = updatedState) }

                    // Auto-guardado periódico
                    if (nextElapsed % 5 == 0) {
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
                statusMessage = if (mode == GameMode.INFILTRATION)
                    "Infiltración en solitario iniciada. Neutraliza la red enemiga."
                else
                    "Cyber Duelo 1v1 iniciado. ¡Defiende tu infraestructura del contraataque!"
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
            saveRepository.saveActiveSession(_uiState.value.gameState)
            loadSavedGamesList()
        }
        _uiState.update { it.copy(currentScreen = CyberScreen.TITLE_MENU) }
    }

    fun onCellClicked(cellIndex: Int) {
        val current = _uiState.value.gameState
        if (current.isGameOver) return

        val timestamp = "Turno ${current.turns + 1}"
        val (nextState, feedback) = GameEngine.executeTurn(current, cellIndex, timestamp)

        // Audio feedback
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
            // CPU nos golpeó
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
