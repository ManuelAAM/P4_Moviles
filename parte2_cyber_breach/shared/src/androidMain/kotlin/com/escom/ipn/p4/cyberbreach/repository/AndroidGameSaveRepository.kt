package com.escom.ipn.p4.cyberbreach.repository

import android.content.Context
import com.escom.ipn.p4.cyberbreach.model.GameAction
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.NetworkNode
import com.escom.ipn.p4.cyberbreach.model.NodeType
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata
import com.escom.ipn.p4.cyberbreach.model.SkillType
import com.escom.ipn.p4.cyberbreach.service.GameSerializationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidGameSaveRepository(
    private val context: Context
) : IGameSaveRepository {

    private val savesDir = File(context.filesDir, "saved_games")
    private val activeSessionFile = File(context.filesDir, "active_session.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        if (!savesDir.exists()) savesDir.mkdirs()
        seedDemoSavesIfEmpty()
    }

    private fun seedDemoSavesIfEmpty() {
        val marker = File(savesDir, ".seeded_saves_v1")
        if (!marker.exists()) {
            try {
                // 1. Demo JSON Save
                val sampleState1 = GameState(
                    gridSize = 8,
                    playerEnergy = 40,
                    score = 850,
                    turns = 12,
                    elapsedSeconds = 120,
                    gameMode = GameMode.INFILTRATION,
                    nodes = listOf(
                        NetworkNode("node_firewall", NodeType.FIREWALL, listOf(10), hits = 1, isCompromised = true),
                        NetworkNode("node_db", NodeType.DATABASE, listOf(24, 25), hits = 1, isCompromised = false),
                        NetworkNode("node_web", NodeType.WEB_SERVER, listOf(40, 41, 42), hits = 0, isCompromised = false),
                        NetworkNode("node_mainframe", NodeType.MAINFRAME, listOf(56, 57, 58, 59), hits = 0, isCompromised = false)
                    ),
                    history = listOf(
                        GameAction(1, "STRIKE", listOf(10), "NODO_DESTRUIDO: Firewall Gateway", "2026-11-04 00:01"),
                        GameAction(2, "STRIKE", listOf(24), "IMPACTO_DETECTADO", "2026-11-04 00:02")
                    )
                )
                val meta1 = SaveMetadata(
                    id = "partida_infiltracion_demo",
                    title = "Operación Centinela (JSON)",
                    categoryTag = "Campaña",
                    format = SaveFormat.JSON,
                    savedAt = "2026-11-04 00:05",
                    score = 850,
                    mode = GameMode.INFILTRATION,
                    turns = 12,
                    elapsedSeconds = 120,
                    isFinished = false,
                    fileName = "partida_infiltracion_demo.json"
                )
                File(savesDir, meta1.fileName).writeText(GameSerializationService.serializeToJson(sampleState1, meta1))

                // 2. Demo XML Save
                val meta2 = SaveMetadata(
                    id = "partida_duelo_cpu_demo",
                    title = "Cyber Duelo vs CPU (XML)",
                    categoryTag = "Duelo CPU",
                    format = SaveFormat.XML,
                    savedAt = "2026-11-04 00:10",
                    score = 1200,
                    mode = GameMode.CPU_DUEL,
                    turns = 18,
                    elapsedSeconds = 45,
                    isFinished = false,
                    fileName = "partida_duelo_cpu_demo.xml"
                )
                File(savesDir, meta2.fileName).writeText(GameSerializationService.serializeToXml(sampleState1.copy(gameMode = GameMode.CPU_DUEL, score = 1200), meta2))

                // 3. Demo TXT Save
                val meta3 = SaveMetadata(
                    id = "partida_record_demo",
                    title = "Infiltración Récord IPN (TXT)",
                    categoryTag = "Récord",
                    format = SaveFormat.TXT,
                    savedAt = "2026-11-04 00:15",
                    score = 2500,
                    mode = GameMode.INFILTRATION,
                    turns = 24,
                    elapsedSeconds = 180,
                    isFinished = true,
                    fileName = "partida_record_demo.txt"
                )
                File(savesDir, meta3.fileName).writeText(GameSerializationService.serializeToTxt(sampleState1.copy(score = 2500, isVictory = true), meta3))

                marker.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveGame(
        state: GameState,
        format: SaveFormat,
        title: String,
        categoryTag: String
    ): Result<SaveMetadata> = withContext(Dispatchers.IO) {
        try {
            val saveId = "cyber_save_${System.currentTimeMillis()}"
            val fileName = "$saveId${format.extension}"
            val timestamp = dateFormat.format(Date())

            val metadata = SaveMetadata(
                id = saveId,
                title = title.ifBlank { "Partida $timestamp" },
                categoryTag = categoryTag.ifBlank { "Campaña" },
                format = format,
                savedAt = timestamp,
                score = state.score,
                mode = state.gameMode,
                turns = state.turns,
                elapsedSeconds = state.elapsedSeconds,
                isFinished = state.isGameOver,
                fileName = fileName
            )

            val content = when (format) {
                SaveFormat.JSON -> GameSerializationService.serializeToJson(state, metadata)
                SaveFormat.XML -> GameSerializationService.serializeToXml(state, metadata)
                SaveFormat.TXT -> GameSerializationService.serializeToTxt(state, metadata)
            }

            val file = File(savesDir, fileName)
            file.writeText(content, Charsets.UTF_8)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadGame(saveId: String): Result<Pair<GameState, SaveMetadata>> = withContext(Dispatchers.IO) {
        try {
            val file = findFileByIdOrName(saveId) ?: return@withContext Result.failure(Exception("Archivo de partida no encontrado."))
            val content = file.readText(Charsets.UTF_8)

            when {
                file.name.endsWith(".json", ignoreCase = true) -> GameSerializationService.deserializeFromJson(content)
                file.name.endsWith(".xml", ignoreCase = true) -> GameSerializationService.deserializeFromXml(content)
                file.name.endsWith(".txt", ignoreCase = true) -> GameSerializationService.deserializeFromTxt(content)
                else -> Result.failure(Exception("Formato no reconocido: ${file.name}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listSaves(): List<SaveMetadata> = withContext(Dispatchers.IO) {
        val files = savesDir.listFiles { f ->
            f.isFile && (f.name.endsWith(".json") || f.name.endsWith(".xml") || f.name.endsWith(".txt"))
        } ?: return@withContext emptyList()

        val results = mutableListOf<SaveMetadata>()
        for (file in files) {
            try {
                val content = file.readText(Charsets.UTF_8)
                val pairResult = when {
                    file.name.endsWith(".json") -> GameSerializationService.deserializeFromJson(content)
                    file.name.endsWith(".xml") -> GameSerializationService.deserializeFromXml(content)
                    file.name.endsWith(".txt") -> GameSerializationService.deserializeFromTxt(content)
                    else -> null
                }
                pairResult?.getOrNull()?.second?.let {
                    results.add(it)
                }
            } catch (e: Exception) {
                // Archivo corrupto o ilegible; se ignora sin romper la lista
            }
        }
        results.sortedByDescending { it.savedAt }
    }

    override suspend fun deleteSave(saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = findFileByIdOrName(saveId) ?: return@withContext Result.failure(Exception("Archivo no encontrado"))
            if (file.delete()) Result.success(Unit) else Result.failure(Exception("No se pudo eliminar el archivo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategoryTag(saveId: String, newTag: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val loadResult = loadGame(saveId)
            val (state, meta) = loadResult.getOrThrow()
            val updatedMeta = meta.copy(categoryTag = newTag)

            val content = when (meta.format) {
                SaveFormat.JSON -> GameSerializationService.serializeToJson(state, updatedMeta)
                SaveFormat.XML -> GameSerializationService.serializeToXml(state, updatedMeta)
                SaveFormat.TXT -> GameSerializationService.serializeToTxt(state, updatedMeta)
            }

            val file = findFileByIdOrName(saveId) ?: return@withContext Result.failure(Exception("Archivo no encontrado"))
            file.writeText(content, Charsets.UTF_8)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readSaveRawContent(saveId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = findFileByIdOrName(saveId) ?: return@withContext Result.failure(Exception("Archivo no encontrado"))
            Result.success(file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveActiveSession(state: GameState): Unit = withContext(Dispatchers.IO) {
        try {
            val meta = SaveMetadata(
                id = "active_session",
                title = "Sesión Activa",
                categoryTag = "En curso",
                format = SaveFormat.JSON,
                savedAt = dateFormat.format(Date()),
                score = state.score,
                mode = state.gameMode,
                turns = state.turns,
                elapsedSeconds = state.elapsedSeconds,
                isFinished = state.isGameOver,
                fileName = "active_session.json"
            )
            val jsonStr = GameSerializationService.serializeToJson(state, meta)
            activeSessionFile.writeText(jsonStr, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loadActiveSession(): GameState? = withContext(Dispatchers.IO) {
        try {
            if (!activeSessionFile.exists()) return@withContext null
            val content = activeSessionFile.readText(Charsets.UTF_8)
            val result = GameSerializationService.deserializeFromJson(content)
            result.getOrNull()?.first
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearActiveSession(): Unit = withContext(Dispatchers.IO) {
        if (activeSessionFile.exists()) activeSessionFile.delete()
    }

    private fun findFileByIdOrName(saveIdOrName: String): File? {
        val direct = File(savesDir, saveIdOrName)
        if (direct.exists()) return direct

        // Buscar por id o nombre
        return savesDir.listFiles()?.find {
            it.name == saveIdOrName || it.nameWithoutExtension == saveIdOrName || it.name.startsWith(saveIdOrName)
        }
    }
}
