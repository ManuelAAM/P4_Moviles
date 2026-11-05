package com.escom.ipn.p4.cyberbreach.repository

import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata

interface IGameSaveRepository {
    suspend fun saveGame(state: GameState, format: SaveFormat, title: String, categoryTag: String): Result<SaveMetadata>
    suspend fun loadGame(saveId: String): Result<Pair<GameState, SaveMetadata>>
    suspend fun listSaves(): List<SaveMetadata>
    suspend fun deleteSave(saveId: String): Result<Unit>
    suspend fun updateCategoryTag(saveId: String, newTag: String): Result<Unit>
    suspend fun readSaveRawContent(saveId: String): Result<String>
    
    // Auto-guardado para recuperación de sesión activa tras cierre de app
    suspend fun saveActiveSession(state: GameState)
    suspend fun loadActiveSession(): GameState?
    suspend fun clearActiveSession()
}
