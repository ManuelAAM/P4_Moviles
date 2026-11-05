package com.escom.ipn.p4.cyberbreach.repository

import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata
import com.escom.ipn.p4.cyberbreach.service.GameSerializationService

class IosGameSaveRepository : IGameSaveRepository {
    private val memorySaves = mutableMapOf<String, String>()
    private val memoryMeta = mutableMapOf<String, SaveMetadata>()
    private var activeSessionState: GameState? = null

    override suspend fun saveGame(
        state: GameState,
        format: SaveFormat,
        title: String,
        categoryTag: String
    ): Result<SaveMetadata> {
        val id = "save_ios_${memorySaves.size + 1}"
        val meta = SaveMetadata(
            id = id,
            title = title,
            categoryTag = categoryTag,
            format = format,
            savedAt = "2026-11-04 00:00",
            score = state.score,
            mode = state.gameMode,
            turns = state.turns,
            elapsedSeconds = state.elapsedSeconds,
            isFinished = state.isGameOver,
            fileName = "$id${format.extension}"
        )
        val content = when (format) {
            SaveFormat.JSON -> GameSerializationService.serializeToJson(state, meta)
            SaveFormat.XML -> GameSerializationService.serializeToXml(state, meta)
            SaveFormat.TXT -> GameSerializationService.serializeToTxt(state, meta)
        }
        memorySaves[id] = content
        memoryMeta[id] = meta
        return Result.success(meta)
    }

    override suspend fun loadGame(saveId: String): Result<Pair<GameState, SaveMetadata>> {
        val content = memorySaves[saveId] ?: return Result.failure(Exception("Partida no encontrada en sandbox de iOS"))
        val meta = memoryMeta[saveId] ?: return Result.failure(Exception("Metadatos no encontrados"))
        return when (meta.format) {
            SaveFormat.JSON -> GameSerializationService.deserializeFromJson(content)
            SaveFormat.XML -> GameSerializationService.deserializeFromXml(content)
            SaveFormat.TXT -> GameSerializationService.deserializeFromTxt(content)
        }
    }

    override suspend fun listSaves(): List<SaveMetadata> {
        return memoryMeta.values.toList()
    }

    override suspend fun deleteSave(saveId: String): Result<Unit> {
        memorySaves.remove(saveId)
        memoryMeta.remove(saveId)
        return Result.success(Unit)
    }

    override suspend fun updateCategoryTag(saveId: String, newTag: String): Result<Unit> {
        val meta = memoryMeta[saveId] ?: return Result.failure(Exception("Partida no encontrada"))
        memoryMeta[saveId] = meta.copy(categoryTag = newTag)
        return Result.success(Unit)
    }

    override suspend fun readSaveRawContent(saveId: String): Result<String> {
        val content = memorySaves[saveId] ?: return Result.failure(Exception("Partida no encontrada"))
        return Result.success(content)
    }

    override suspend fun saveActiveSession(state: GameState) {
        activeSessionState = state
    }

    override suspend fun loadActiveSession(): GameState? {
        return activeSessionState
    }

    override suspend fun clearActiveSession() {
        activeSessionState = null
    }
}
