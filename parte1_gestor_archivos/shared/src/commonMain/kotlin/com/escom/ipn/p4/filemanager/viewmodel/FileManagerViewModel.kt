package com.escom.ipn.p4.filemanager.viewmodel

import com.escom.ipn.p4.filemanager.model.AppColorTheme
import com.escom.ipn.p4.filemanager.model.BiometricAuthResult
import com.escom.ipn.p4.filemanager.model.BluetoothPeerDevice
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.FileTransferRecord
import com.escom.ipn.p4.filemanager.model.HttpProxyResponse
import com.escom.ipn.p4.filemanager.model.IBiometricAuthService
import com.escom.ipn.p4.filemanager.model.ISensorManager
import com.escom.ipn.p4.filemanager.model.MotionTiltState
import com.escom.ipn.p4.filemanager.model.SensorData
import com.escom.ipn.p4.filemanager.model.SensorType
import com.escom.ipn.p4.filemanager.model.WebCacheRecord
import com.escom.ipn.p4.filemanager.repository.IFileManagerRepository
import com.escom.ipn.p4.filemanager.service.IBluetoothTransferService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BreadcrumbItem(
    val title: String,
    val path: String
)

data class FileManagerUiState(
    val currentPath: String = "",
    val breadcrumbs: List<BreadcrumbItem> = emptyList(),
    val items: List<FileItem> = emptyList(),
    val filteredItems: List<FileItem> = emptyList(),
    val searchQuery: String = "",
    val isFavoritesOnly: Boolean = false,
    val isRecentsOnly: Boolean = false,
    val isLoading: Boolean = false,
    val selectedFileForViewing: FileItem? = null,
    val viewingContent: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val colorTheme: AppColorTheme = AppColorTheme.GUINDA_IPN,
    val isDarkThemeForced: Boolean? = null,

    // Ejercicio 1: Telemetría de Sensores y Biometría
    val sensorDataAccel: SensorData = SensorData(SensorType.ACCELEROMETER),
    val sensorDataGyro: SensorData = SensorData(SensorType.GYROSCOPE),
    val isSensorSimulationEnabled: Boolean = false,
    val tiltState: MotionTiltState = MotionTiltState(),
    val showSensorTelemetryDialog: Boolean = false,
    val isVaultUnlocked: Boolean = false,

    // Ejercicio 2: Transferencia Bluetooth
    val showBluetoothTransferDialog: Boolean = false,
    val selectedFileForTransfer: FileItem? = null,
    val bluetoothDevices: List<BluetoothPeerDevice> = emptyList(),
    val connectedPeer: BluetoothPeerDevice? = null,
    val activeTransfer: FileTransferRecord? = null,
    val transferHistory: List<FileTransferRecord> = emptyList(),
    val pendingIncomingOffer: FileTransferMetadataPacket? = null,

    // Ejercicio 3: Consulta Web Proxy por Bluetooth
    val showWebProxyDialog: Boolean = false,
    val proxyUrlInput: String = "https://jsonplaceholder.typicode.com/posts/1",
    val lastProxyResponse: HttpProxyResponse? = null,
    val webCacheRecords: List<WebCacheRecord> = emptyList(),
    val isProxyLoading: Boolean = false
)

class FileManagerViewModel(
    private val repository: IFileManagerRepository,
    private val sensorManager: ISensorManager? = null,
    private val biometricService: IBiometricAuthService? = null,
    private val bluetoothService: IBluetoothTransferService? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    private var rootDirectories: List<FileItem> = emptyList()

    init {
        initializeRoots()
        bindSensorFlows()
        bindBluetoothFlows()
    }

    private fun bindSensorFlows() {
        sensorManager?.let { sm ->
            coroutineScope.launch {
                sm.accelerometerFlow.collect { data ->
                    _uiState.update { it.copy(sensorDataAccel = data) }
                }
            }
            coroutineScope.launch {
                sm.gyroscopeFlow.collect { data ->
                    _uiState.update { it.copy(sensorDataGyro = data) }
                }
            }
            coroutineScope.launch {
                sm.shakeEventFlow.collect { timestamp ->
                    if (timestamp > 0L) {
                        refreshCurrentDirectory()
                        _uiState.update {
                            it.copy(statusMessage = "¡Gesto de agitación (Shake) detectado! Directorio actualizado.")
                        }
                    }
                }
            }
            coroutineScope.launch {
                sm.tiltStateFlow.collect { tilt ->
                    _uiState.update { it.copy(tiltState = tilt) }
                }
            }
            sm.startListening(60L)
        }
    }

    private fun bindBluetoothFlows() {
        bluetoothService?.let { bt ->
            coroutineScope.launch {
                bt.bondedDevicesFlow.collect { devs ->
                    _uiState.update { it.copy(bluetoothDevices = devs) }
                }
            }
            coroutineScope.launch {
                bt.connectedPeerFlow.collect { peer ->
                    _uiState.update { it.copy(connectedPeer = peer) }
                }
            }
            coroutineScope.launch {
                bt.activeTransferFlow.collect { act ->
                    _uiState.update { it.copy(activeTransfer = act) }
                }
            }
            coroutineScope.launch {
                bt.transferHistoryFlow.collect { hist ->
                    _uiState.update { it.copy(transferHistory = hist) }
                }
            }
            coroutineScope.launch {
                bt.pendingIncomingOfferFlow.collect { offer ->
                    _uiState.update { it.copy(pendingIncomingOffer = offer) }
                }
            }
            coroutineScope.launch {
                bt.proxyResponseFlow.collect { res ->
                    _uiState.update { it.copy(lastProxyResponse = res, isProxyLoading = false) }
                }
            }
            coroutineScope.launch {
                bt.webCacheFlow.collect { cache ->
                    _uiState.update { it.copy(webCacheRecords = cache) }
                }
            }
        }
    }

    fun initializeRoots() {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val roots = repository.getRootDirectories()
            rootDirectories = roots

            if (roots.isNotEmpty()) {
                val primaryRoot = roots.first()
                val crumbs = listOf(BreadcrumbItem(primaryRoot.name, primaryRoot.path))
                val items = repository.listFiles(primaryRoot.path)
                _uiState.update {
                    it.copy(
                        currentPath = primaryRoot.path,
                        breadcrumbs = crumbs,
                        items = items,
                        filteredItems = items,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontraron directorios accesibles.") }
            }
        }
    }

    fun navigateTo(item: FileItem) {
        if (!item.isDirectory) {
            openFile(item)
            return
        }

        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = "", isFavoritesOnly = false, isRecentsOnly = false) }
            val files = repository.listFiles(item.path)
            val updatedCrumbs = _uiState.value.breadcrumbs.toMutableList().apply {
                add(BreadcrumbItem(item.name, item.path))
            }
            _uiState.update {
                it.copy(
                    currentPath = item.path,
                    breadcrumbs = updatedCrumbs,
                    items = files,
                    filteredItems = files,
                    isLoading = false
                )
            }
        }
    }

    fun navigateBreadcrumb(index: Int) {
        val crumbs = _uiState.value.breadcrumbs
        if (index < 0 || index >= crumbs.size) return
        val target = crumbs[index]
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = "", isFavoritesOnly = false, isRecentsOnly = false) }
            val files = repository.listFiles(target.path)
            val updatedCrumbs = crumbs.subList(0, index + 1)
            _uiState.update {
                it.copy(
                    currentPath = target.path,
                    breadcrumbs = updatedCrumbs,
                    items = files,
                    filteredItems = files,
                    isLoading = false
                )
            }
        }
    }

    fun navigateUp(): Boolean {
        val crumbs = _uiState.value.breadcrumbs
        if (crumbs.size > 1) {
            navigateBreadcrumb(crumbs.size - 2)
            return true
        }
        return false
    }

    fun refreshCurrentDirectory() {
        val path = _uiState.value.currentPath
        if (path.isEmpty()) return

        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val files = repository.listFiles(path)
            _uiState.update { state ->
                val filtered = applyFilter(files, state.searchQuery, state.isFavoritesOnly, state.isRecentsOnly)
                state.copy(items = files, filteredItems = filtered, isLoading = false)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilter(state.items, query, state.isFavoritesOnly, state.isRecentsOnly)
            state.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    fun toggleFavoritesFilter() {
        _uiState.update { state ->
            val next = !state.isFavoritesOnly
            val filtered = applyFilter(state.items, state.searchQuery, next, false)
            state.copy(isFavoritesOnly = next, isRecentsOnly = false, filteredItems = filtered)
        }
    }

    fun toggleRecentsFilter() {
        _uiState.update { state ->
            val next = !state.isRecentsOnly
            val filtered = applyFilter(state.items, state.searchQuery, false, next)
            state.copy(isRecentsOnly = next, isFavoritesOnly = false, filteredItems = filtered)
        }
    }

    private fun applyFilter(
        items: List<FileItem>,
        query: String,
        favoritesOnly: Boolean,
        recentsOnly: Boolean
    ): List<FileItem> {
        var result = items
        if (favoritesOnly) {
            val favs = repository.getFavorites()
            result = result.filter { favs.contains(it.path) }
        } else if (recentsOnly) {
            val recs = repository.getRecents().toSet()
            result = result.filter { recs.contains(it.path) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { it.name.lowercase().contains(q) }
        }
        return result
    }

    fun toggleFavorite(item: FileItem) {
        val newState = repository.toggleFavorite(item.path)
        _uiState.update { state ->
            val updatedItems = state.items.map {
                if (it.path == item.path) it.copy(isFavorite = newState) else it
            }
            val filtered = applyFilter(updatedItems, state.searchQuery, state.isFavoritesOnly, state.isRecentsOnly)
            state.copy(items = updatedItems, filteredItems = filtered)
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre de la carpeta no puede estar vacío.") }
            return
        }
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.createDirectory(_uiState.value.currentPath, name.trim())
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Carpeta '$name' creada con éxito.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al crear carpeta.") }
                }
            )
        }
    }

    fun createTextFile(name: String, content: String) {
        val cleanName = if (!name.contains('.')) "$name.txt" else name
        val path = "${_uiState.value.currentPath}/$cleanName"
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.writeFileText(path, content)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Archivo '$cleanName' guardado con éxito.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al guardar archivo.") }
                }
            )
        }
    }

    fun renameItem(item: FileItem, newName: String) {
        if (newName.isBlank() || newName == item.name) return
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.renameItem(item.path, newName.trim())
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Renombrado a '$newName' correctamente.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al renombrar.") }
                }
            )
        }
    }

    fun deleteItem(item: FileItem) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteItem(item.path)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Elemento eliminado correctamente.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al eliminar.") }
                }
            )
        }
    }

    fun copyItem(item: FileItem, targetDir: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.copyItem(item.path, targetDir)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Copia realizada correctamente.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al copiar.") }
                }
            )
        }
    }

    fun moveItem(item: FileItem, targetDir: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.moveItem(item.path, targetDir)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Elemento movido correctamente.") }
                    refreshCurrentDirectory()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error al mover.") }
                }
            )
        }
    }

    fun openFile(item: FileItem) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedFileForViewing = item) }
            val readResult = repository.readFileText(item.path)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    viewingContent = readResult.getOrNull() ?: "(No se pudo leer el contenido como texto)",
                    selectedFileForViewing = item
                )
            }
        }
    }

    fun closeFileViewer() {
        _uiState.update { it.copy(selectedFileForViewing = null, viewingContent = null) }
    }

    fun setColorTheme(theme: AppColorTheme) {
        _uiState.update { it.copy(colorTheme = theme) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }

    // ==========================================
    // EJERCICIO 1: SENSORES Y BIOMETRÍA
    // ==========================================

    fun openSensorTelemetryDialog() {
        sensorManager?.startListening(60L)
        _uiState.update { it.copy(showSensorTelemetryDialog = true) }
    }

    fun closeSensorTelemetryDialog() {
        _uiState.update { it.copy(showSensorTelemetryDialog = false) }
    }

    fun toggleSensorSimulation() {
        val next = !_uiState.value.isSensorSimulationEnabled
        sensorManager?.setSimulationMode(next)
        _uiState.update { it.copy(isSensorSimulationEnabled = next) }
    }

    fun injectSimulatedSensorReading(type: SensorType, x: Float, y: Float, z: Float) {
        sensorManager?.injectSimulatedReading(type, x, y, z)
    }

    fun unlockVaultWithBiometrics(onSuccess: () -> Unit = {}) {
        val bio = biometricService
        if (bio == null) {
            _uiState.update { it.copy(isVaultUnlocked = true, statusMessage = "Bóveda abierta en modo simulación.") }
            onSuccess()
            return
        }

        coroutineScope.launch {
            val result = bio.authenticate(
                title = "Acceso a Bóveda Protegida",
                subtitle = "Autenticación Biométrica IPN-ESCOM",
                description = "Confirme su identidad con huella dactilar o PIN del dispositivo para ver archivos confidenciales."
            )
            when (result) {
                BiometricAuthResult.SUCCESS, BiometricAuthResult.DEVICE_CREDENTIAL_SUCCESS -> {
                    _uiState.update {
                        it.copy(isVaultUnlocked = true, statusMessage = "Autenticación biométrica exitosa. Bóveda desbloqueada.")
                    }
                    onSuccess()
                }
                BiometricAuthResult.CANCELED -> {
                    _uiState.update { it.copy(statusMessage = "Operación biométrica cancelada.") }
                }
                BiometricAuthResult.NOT_AVAILABLE -> {
                    _uiState.update {
                        it.copy(isVaultUnlocked = true, statusMessage = "Biometría no configurada; accediendo con credencial segura.")
                    }
                    onSuccess()
                }
                BiometricAuthResult.FAILED -> {
                    _uiState.update { it.copy(errorMessage = "Huella no reconocida. Acceso denegado a la bóveda.") }
                }
            }
        }
    }

    fun lockVault() {
        _uiState.update { it.copy(isVaultUnlocked = false, statusMessage = "Bóveda protegida bloqueada.") }
    }

    // ==========================================
    // EJERCICIO 2: TRANSFERENCIA POR BLUETOOTH
    // ==========================================

    fun openBluetoothTransferDialog(item: FileItem? = null) {
        bluetoothService?.refreshDevices()
        _uiState.update {
            it.copy(
                showBluetoothTransferDialog = true,
                selectedFileForTransfer = item
            )
        }
    }

    fun closeBluetoothTransferDialog() {
        _uiState.update {
            it.copy(
                showBluetoothTransferDialog = false,
                selectedFileForTransfer = null
            )
        }
    }

    fun connectToBluetoothDevice(device: BluetoothPeerDevice) {
        bluetoothService?.connectToDevice(device)
    }

    fun sendFileViaBluetooth(fileItem: FileItem, device: BluetoothPeerDevice) {
        val bt = bluetoothService ?: return
        coroutineScope.launch {
            val bytes = repository.readFileBytes(fileItem.path).getOrNull()
                ?: repository.readFileText(fileItem.path).getOrNull()?.encodeToByteArray()
                ?: ByteArray(0)
            val result = bt.sendFile(device, fileItem.name, fileItem.mimeType, bytes)
            result.fold(
                onSuccess = { record ->
                    _uiState.update {
                        it.copy(statusMessage = "Transferencia de '${record.fileName}' iniciada hacia ${device.name}.")
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(errorMessage = "Error al enviar archivo por Bluetooth: ${err.message}")
                    }
                }
            )
        }
    }

    fun simulateBluetoothTransfer(fileItem: FileItem) {
        coroutineScope.launch {
            val bytes = repository.readFileBytes(fileItem.path).getOrNull()
                ?: repository.readFileText(fileItem.path).getOrNull()?.encodeToByteArray()
                ?: "Demo KMP File".encodeToByteArray()
            bluetoothService?.simulateTransfer(fileItem.name, fileItem.mimeType, bytes, asSender = true)
            _uiState.update {
                it.copy(statusMessage = "Simulando envío por bloques de '${fileItem.name}'...")
            }
        }
    }

    fun simulateIncomingTransferOffer() {
        val demoOffer = FileTransferMetadataPacket(
            transferId = "demo_inc_" + (0..9999).random(),
            fileName = "alumnos_escom.json",
            fileExtension = "json",
            mimeType = "application/json",
            totalBytes = 2048L,
            sha256 = "b7a8291a134d193d94828131d2e16447c29373e27150a8b9e69dbfbf6e85d1e4",
            senderName = "Galaxy A54 (Nodo Par)"
        )
        bluetoothService?.injectIncomingOffer(demoOffer)
        _uiState.update { it.copy(statusMessage = "Simulando oferta entrante con SHA-256...") }
    }

    fun acceptIncomingTransfer() {
        coroutineScope.launch {
            bluetoothService?.acceptIncomingTransfer()?.fold(
                onSuccess = {
                    refreshCurrentDirectory()
                    _uiState.update { it.copy(statusMessage = "Archivo recibido e integridad SHA-256 verificada con éxito.") }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = "Fallo en recepción: ${err.message}") }
                }
            )
        }
    }

    fun rejectIncomingTransfer() {
        bluetoothService?.rejectIncomingTransfer()
        _uiState.update { it.copy(statusMessage = "Transferencia entrante rechazada.") }
    }

    fun cancelActiveTransfer() {
        bluetoothService?.cancelActiveTransfer()
        _uiState.update { it.copy(statusMessage = "Transferencia cancelada.") }
    }

    // ==========================================
    // EJERCICIO 3: CONSULTA WEB POR BLUETOOTH
    // ==========================================

    fun openWebProxyDialog() {
        _uiState.update { it.copy(showWebProxyDialog = true) }
    }

    fun closeWebProxyDialog() {
        _uiState.update { it.copy(showWebProxyDialog = false) }
    }

    fun executeWebProxyRequest(url: String) {
        val bt = bluetoothService ?: return
        coroutineScope.launch {
            _uiState.update { it.copy(isProxyLoading = true, proxyUrlInput = url) }
            val response = bt.sendProxyRequest(url)
            _uiState.update {
                it.copy(
                    isProxyLoading = false,
                    lastProxyResponse = response,
                    statusMessage = if (response.fromCache)
                        "Respuesta cargada desde la memoria caché local (Offline)."
                    else
                        "Respuesta remota recibida a través del Gateway Bluetooth."
                )
            }
        }
    }

    fun clearWebProxyCache() {
        bluetoothService?.clearCache()
        _uiState.update { it.copy(statusMessage = "Caché de consultas web vaciada con éxito.") }
    }
}
