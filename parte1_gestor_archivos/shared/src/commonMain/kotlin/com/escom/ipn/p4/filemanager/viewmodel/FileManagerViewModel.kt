package com.escom.ipn.p4.filemanager.viewmodel

import com.escom.ipn.p4.filemanager.model.AppColorTheme
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.repository.IFileManagerRepository
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
    val isDarkThemeForced: Boolean? = null // null means system default
)

class FileManagerViewModel(
    private val repository: IFileManagerRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    private var rootDirectories: List<FileItem> = emptyList()

    init {
        initializeRoots()
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
}
