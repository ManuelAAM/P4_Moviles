package com.escom.ipn.p4.cyberbreach.service

import com.escom.ipn.p4.cyberbreach.model.GameAction
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.NetworkNode
import com.escom.ipn.p4.cyberbreach.model.NodeType
import com.escom.ipn.p4.cyberbreach.model.SaveFormat
import com.escom.ipn.p4.cyberbreach.model.SaveMetadata
import com.escom.ipn.p4.cyberbreach.model.SkillType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CyberBreachSaveContainer(
    val metadata: SaveMetadata,
    val state: GameState
)

object GameSerializationService {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ==========================================
    // JSON SERIALIZATION / DESERIALIZATION
    // ==========================================
    fun serializeToJson(state: GameState, metadata: SaveMetadata): String {
        val container = CyberBreachSaveContainer(metadata = metadata, state = state)
        return json.encodeToString(container)
    }

    fun deserializeFromJson(content: String): Result<Pair<GameState, SaveMetadata>> {
        if (content.isBlank()) return Result.failure(Exception("El archivo JSON está completamente vacío."))
        return try {
            val container = json.decodeFromString<CyberBreachSaveContainer>(content)
            validateGameState(container.state)
            Result.success(Pair(container.state, container.metadata))
        } catch (e: Exception) {
            Result.failure(Exception("Archivo JSON corrupto o incompleto: ${e.message}"))
        }
    }

    // ==========================================
    // XML SERIALIZATION / DESERIALIZATION
    // ==========================================
    fun serializeToXml(state: GameState, metadata: SaveMetadata): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<CyberBreachSave>\n")

        // Metadata
        sb.append("  <Metadata>\n")
        sb.append("    <Id>${escapeXml(metadata.id)}</Id>\n")
        sb.append("    <Title>${escapeXml(metadata.title)}</Title>\n")
        sb.append("    <CategoryTag>${escapeXml(metadata.categoryTag)}</CategoryTag>\n")
        sb.append("    <Format>${metadata.format.name}</Format>\n")
        sb.append("    <SavedAt>${escapeXml(metadata.savedAt)}</SavedAt>\n")
        sb.append("    <Score>${metadata.score}</Score>\n")
        sb.append("    <Mode>${metadata.mode.name}</Mode>\n")
        sb.append("    <Turns>${metadata.turns}</Turns>\n")
        sb.append("    <ElapsedSeconds>${metadata.elapsedSeconds}</ElapsedSeconds>\n")
        sb.append("    <IsFinished>${metadata.isFinished}</IsFinished>\n")
        sb.append("    <FileName>${escapeXml(metadata.fileName)}</FileName>\n")
        sb.append("  </Metadata>\n")

        // GameState
        sb.append("  <GameState>\n")
        sb.append("    <GridSize>${state.gridSize}</GridSize>\n")
        sb.append("    <GridCells>${state.grid.joinToString(",")}</GridCells>\n")
        sb.append("    <PlayerGridCells>${state.playerGrid.joinToString(",")}</PlayerGridCells>\n")
        sb.append("    <PlayerEnergy>${state.playerEnergy}</PlayerEnergy>\n")
        sb.append("    <Score>${state.score}</Score>\n")
        sb.append("    <Turns>${state.turns}</Turns>\n")
        sb.append("    <ElapsedSeconds>${state.elapsedSeconds}</ElapsedSeconds>\n")
        sb.append("    <TimeRemaining>${state.timeRemainingSeconds}</TimeRemaining>\n")
        sb.append("    <GameMode>${state.gameMode.name}</GameMode>\n")
        sb.append("    <IsVictory>${state.isVictory}</IsVictory>\n")
        sb.append("    <IsDefeat>${state.isDefeat}</IsDefeat>\n")
        sb.append("  </GameState>\n")

        // Enemy Nodes
        sb.append("  <Nodes>\n")
        state.nodes.forEach { node ->
            sb.append("    <Node id=\"${node.id}\" type=\"${node.type.name}\" hits=\"${node.hits}\" compromised=\"${node.isCompromised}\">\n")
            sb.append("      <Cells>${node.cells.joinToString(",")}</Cells>\n")
            sb.append("    </Node>\n")
        }
        sb.append("  </Nodes>\n")

        // Player Nodes (if Duel mode)
        if (state.playerNodes.isNotEmpty()) {
            sb.append("  <PlayerNodes>\n")
            state.playerNodes.forEach { node ->
                sb.append("    <Node id=\"${node.id}\" type=\"${node.type.name}\" hits=\"${node.hits}\" compromised=\"${node.isCompromised}\">\n")
                sb.append("      <Cells>${node.cells.joinToString(",")}</Cells>\n")
                sb.append("    </Node>\n")
            }
            sb.append("  </PlayerNodes>\n")
        }

        // Action History
        sb.append("  <ActionHistory>\n")
        state.history.forEach { act ->
            sb.append("    <Action turn=\"${act.turn}\" type=\"${act.actionType}\" targets=\"${act.targetIndices.joinToString(",")}\" result=\"${escapeXml(act.result)}\" time=\"${escapeXml(act.timestamp)}\" />\n")
        }
        sb.append("  </ActionHistory>\n")

        sb.append("</CyberBreachSave>")
        return sb.toString()
    }

    fun deserializeFromXml(content: String): Result<Pair<GameState, SaveMetadata>> {
        if (content.isBlank()) return Result.failure(Exception("El archivo XML está completamente vacío."))
        return try {
            if (!content.contains("<CyberBreachSave>") || !content.contains("</CyberBreachSave>")) {
                return Result.failure(Exception("Etiqueta raíz <CyberBreachSave> no encontrada o archivo incompleto."))
            }

            // Extract Metadata
            val id = extractTag(content, "Id") ?: "save_${kotlin.random.Random.nextInt(1000)}"
            val title = extractTag(content, "Title") ?: "Partida sin título"
            val category = extractTag(content, "CategoryTag") ?: "Campaña"
            val savedAt = extractTag(content, "SavedAt") ?: ""
            val score = extractTag(content, "Score")?.toIntOrNull() ?: 0
            val modeStr = extractTag(content, "Mode") ?: "INFILTRATION"
            val mode = if (modeStr == "CPU_DUEL") GameMode.CPU_DUEL else GameMode.INFILTRATION
            val turns = extractTag(content, "Turns")?.toIntOrNull() ?: 0
            val elapsed = extractTag(content, "ElapsedSeconds")?.toIntOrNull() ?: 0
            val isFinished = extractTag(content, "IsFinished")?.toBooleanStrictOrNull() ?: false
            val fileName = extractTag(content, "FileName") ?: "$id.xml"

            val metadata = SaveMetadata(
                id = id,
                title = title,
                categoryTag = category,
                format = SaveFormat.XML,
                savedAt = savedAt,
                score = score,
                mode = mode,
                turns = turns,
                elapsedSeconds = elapsed,
                isFinished = isFinished,
                fileName = fileName
            )

            // Extract GameState
            val gridSize = extractTag(content, "GridSize")?.toIntOrNull() ?: 8
            val rawGrid = extractTag(content, "GridCells") ?: return Result.failure(Exception("Sección <GridCells> no encontrada en XML."))
            val grid = rawGrid.split(",").filter { it.isNotBlank() }.map { it.trim().toInt() }
            if (grid.size != 64) {
                return Result.failure(Exception("Cuadrícula corrupta en XML: contiene ${grid.size} celdas en vez de 64."))
            }

            val rawPlayerGrid = extractTag(content, "PlayerGridCells")
            val playerGrid = if (rawPlayerGrid != null) {
                rawPlayerGrid.split(",").filter { it.isNotBlank() }.map { it.trim().toInt() }
            } else List(64) { 0 }

            val playerEnergy = extractTag(content, "PlayerEnergy")?.toIntOrNull() ?: 0
            val timeRemaining = extractTag(content, "TimeRemaining")?.toIntOrNull() ?: 90
            val isVictory = extractTag(content, "IsVictory")?.toBooleanStrictOrNull() ?: false
            val isDefeat = extractTag(content, "IsDefeat")?.toBooleanStrictOrNull() ?: false

            // Extract Enemy Nodes
            val nodes = parseNodesSection(content, "<Nodes>", "</Nodes>")
            // Extract Player Nodes (if any)
            val playerNodes = parseNodesSection(content, "<PlayerNodes>", "</PlayerNodes>")

            // Extract Actions
            val history = mutableListOf<GameAction>()
            val actionRegex = Regex("<Action\\s+turn=\"([^\"]+)\"\\s+type=\"([^\"]+)\"\\s+targets=\"([^\"]*)\"\\s+result=\"([^\"]*)\"\\s+time=\"([^\"]*)\"\\s*/>")
            actionRegex.findAll(content).forEach { match ->
                val (aTurn, aType, aTargets, aResult, aTime) = match.destructured
                val targets = if (aTargets.isBlank()) emptyList() else aTargets.split(",").map { it.trim().toInt() }
                history.add(
                    GameAction(
                        turn = aTurn.toIntOrNull() ?: 0,
                        actionType = aType,
                        targetIndices = targets,
                        result = unescapeXml(aResult),
                        timestamp = unescapeXml(aTime)
                    )
                )
            }

            val state = GameState(
                gridSize = gridSize,
                grid = grid,
                nodes = nodes,
                playerGrid = if (playerGrid.size == 64) playerGrid else List(64) { 0 },
                playerNodes = playerNodes,
                playerEnergy = playerEnergy,
                score = score,
                turns = turns,
                elapsedSeconds = elapsed,
                timeRemainingSeconds = timeRemaining,
                gameMode = mode,
                isVictory = isVictory,
                isDefeat = isDefeat,
                history = history,
                activeSkill = SkillType.NORMAL
            )

            validateGameState(state)
            Result.success(Pair(state, metadata))
        } catch (e: Exception) {
            Result.failure(Exception("Error al parsear archivo XML: ${e.message}"))
        }
    }

    private fun parseNodesSection(content: String, startTag: String, endTag: String): List<NetworkNode> {
        val start = content.indexOf(startTag)
        if (start == -1) return emptyList()
        val end = content.indexOf(endTag, start)
        if (end == -1) return emptyList()
        val section = content.substring(start, end + endTag.length)

        val nodes = mutableListOf<NetworkNode>()
        val nodeRegex = Regex("<Node\\s+id=\"([^\"]+)\"\\s+type=\"([^\"]+)\"\\s+hits=\"([^\"]+)\"\\s+compromised=\"([^\"]+)\">\\s*<Cells>([^<]*)</Cells>\\s*</Node>")
        nodeRegex.findAll(section).forEach { match ->
            val (nId, nType, nHits, nCompromised, nCells) = match.destructured
            val type = NodeType.valueOf(nType)
            val cells = nCells.split(",").filter { it.isNotBlank() }.map { it.trim().toInt() }
            nodes.add(
                NetworkNode(
                    id = nId,
                    type = type,
                    cells = cells,
                    hits = nHits.toIntOrNull() ?: 0,
                    isCompromised = nCompromised.toBooleanStrictOrNull() ?: false
                )
            )
        }
        return nodes
    }

    // ==========================================
    // TXT SERIALIZATION / DESERIALIZATION
    // ==========================================
    fun serializeToTxt(state: GameState, metadata: SaveMetadata): String {
        val sb = StringBuilder()
        sb.append("# ====================================================\n")
        sb.append("# CYBER BREACH (NET STRIKE) - PARTIDA GUARDADA\n")
        sb.append("# ====================================================\n\n")

        sb.append("[METADATA]\n")
        sb.append("id=${metadata.id}\n")
        sb.append("title=${metadata.title}\n")
        sb.append("category=${metadata.categoryTag}\n")
        sb.append("format=TXT\n")
        sb.append("saved_at=${metadata.savedAt}\n")
        sb.append("score=${metadata.score}\n")
        sb.append("mode=${metadata.mode.name}\n")
        sb.append("turns=${metadata.turns}\n")
        sb.append("elapsed_seconds=${metadata.elapsedSeconds}\n")
        sb.append("is_finished=${metadata.isFinished}\n")
        sb.append("file_name=${metadata.fileName}\n\n")

        sb.append("[GAME_STATE]\n")
        sb.append("grid_size=${state.gridSize}\n")
        sb.append("player_energy=${state.playerEnergy}\n")
        sb.append("score=${state.score}\n")
        sb.append("turns=${state.turns}\n")
        sb.append("elapsed_seconds=${state.elapsedSeconds}\n")
        sb.append("time_remaining=${state.timeRemainingSeconds}\n")
        sb.append("game_mode=${state.gameMode.name}\n")
        sb.append("is_victory=${state.isVictory}\n")
        sb.append("is_defeat=${state.isDefeat}\n\n")

        sb.append("[GRID_MATRIX]\n")
        for (r in 0 until state.gridSize) {
            val rowCells = (0 until state.gridSize).map { c -> state.grid[r * state.gridSize + c] }
            sb.append(rowCells.joinToString(",")).append("\n")
        }
        sb.append("\n")

        if (state.playerNodes.isNotEmpty()) {
            sb.append("[PLAYER_GRID_MATRIX]\n")
            for (r in 0 until state.gridSize) {
                val rowCells = (0 until state.gridSize).map { c -> state.playerGrid[r * state.gridSize + c] }
                sb.append(rowCells.joinToString(",")).append("\n")
            }
            sb.append("\n")
        }

        sb.append("[NODES]\n")
        state.nodes.forEach { node ->
            sb.append("${node.id}|${node.type.name}|${node.hits}|${node.isCompromised}|${node.cells.joinToString(",")}\n")
        }
        sb.append("\n")

        if (state.playerNodes.isNotEmpty()) {
            sb.append("[PLAYER_NODES]\n")
            state.playerNodes.forEach { node ->
                sb.append("${node.id}|${node.type.name}|${node.hits}|${node.isCompromised}|${node.cells.joinToString(",")}\n")
            }
            sb.append("\n")
        }

        sb.append("[ACTION_HISTORY]\n")
        state.history.forEach { act ->
            sb.append("${act.turn}|${act.actionType}|${act.targetIndices.joinToString(",")}|${act.result}|${act.timestamp}\n")
        }

        return sb.toString()
    }

    fun deserializeFromTxt(content: String): Result<Pair<GameState, SaveMetadata>> {
        if (content.isBlank()) return Result.failure(Exception("El archivo TXT está vacío."))
        return try {
            val sections = mutableMapOf<String, MutableList<String>>()
            var currentSection = ""

            content.lines().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.startsWith("#") || line.isEmpty()) return@forEach
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length - 1).uppercase()
                    sections[currentSection] = mutableListOf()
                } else if (currentSection.isNotEmpty()) {
                    sections[currentSection]?.add(line)
                }
            }

            val metaLines = sections["METADATA"] ?: return Result.failure(Exception("Falta la sección obligatoria [METADATA] en el archivo TXT."))
            val metaMap = metaLines.associate {
                val parts = it.split("=", limit = 2)
                parts[0].trim() to parts.getOrElse(1) { "" }.trim()
            }

            val id = metaMap["id"] ?: "save_${kotlin.random.Random.nextInt(1000)}"
            val title = metaMap["title"] ?: "Partida TXT"
            val category = metaMap["category"] ?: "Campaña"
            val savedAt = metaMap["saved_at"] ?: ""
            val score = metaMap["score"]?.toIntOrNull() ?: 0
            val modeStr = metaMap["mode"] ?: "INFILTRATION"
            val mode = if (modeStr == "CPU_DUEL") GameMode.CPU_DUEL else GameMode.INFILTRATION
            val turns = metaMap["turns"]?.toIntOrNull() ?: 0
            val elapsed = metaMap["elapsed_seconds"]?.toIntOrNull() ?: 0
            val isFinished = metaMap["is_finished"]?.toBooleanStrictOrNull() ?: false
            val fileName = metaMap["file_name"] ?: "$id.txt"

            val metadata = SaveMetadata(
                id = id,
                title = title,
                categoryTag = category,
                format = SaveFormat.TXT,
                savedAt = savedAt,
                score = score,
                mode = mode,
                turns = turns,
                elapsedSeconds = elapsed,
                isFinished = isFinished,
                fileName = fileName
            )

            val stateLines = sections["GAME_STATE"] ?: return Result.failure(Exception("Falta la sección obligatoria [GAME_STATE] en el archivo TXT."))
            val stateMap = stateLines.associate {
                val parts = it.split("=", limit = 2)
                parts[0].trim() to parts.getOrElse(1) { "" }.trim()
            }

            val gridSize = stateMap["grid_size"]?.toIntOrNull() ?: 8
            val playerEnergy = stateMap["player_energy"]?.toIntOrNull() ?: 0
            val timeRemaining = stateMap["time_remaining"]?.toIntOrNull() ?: 90
            val isVictory = stateMap["is_victory"]?.toBooleanStrictOrNull() ?: false
            val isDefeat = stateMap["is_defeat"]?.toBooleanStrictOrNull() ?: false

            // Grid Matrix
            val gridLines = sections["GRID_MATRIX"] ?: return Result.failure(Exception("Falta la sección [GRID_MATRIX] en el archivo TXT."))
            val grid = mutableListOf<Int>()
            gridLines.forEach { rowStr ->
                rowStr.split(",").filter { it.isNotBlank() }.forEach {
                    grid.add(it.trim().toInt())
                }
            }
            if (grid.size != 64) {
                return Result.failure(Exception("Matriz inválida en TXT: se esperaban 64 celdas, pero se obtuvieron ${grid.size}."))
            }

            // Player Grid Matrix (optional)
            val playerGrid = mutableListOf<Int>()
            sections["PLAYER_GRID_MATRIX"]?.forEach { rowStr ->
                rowStr.split(",").filter { it.isNotBlank() }.forEach {
                    playerGrid.add(it.trim().toInt())
                }
            }

            // Nodes
            val nodeLines = sections["NODES"] ?: emptyList()
            val nodes = parseTxtNodes(nodeLines)

            // Player Nodes
            val playerNodeLines = sections["PLAYER_NODES"] ?: emptyList()
            val playerNodes = parseTxtNodes(playerNodeLines)

            // History
            val historyLines = sections["ACTION_HISTORY"] ?: emptyList()
            val history = mutableListOf<GameAction>()
            historyLines.forEach { line ->
                val p = line.split("|")
                if (p.size >= 5) {
                    val hTurn = p[0].toIntOrNull() ?: 0
                    val hType = p[1]
                    val hTargets = if (p[2].isBlank()) emptyList() else p[2].split(",").map { it.trim().toInt() }
                    val hResult = p[3]
                    val hTime = p[4]
                    history.add(GameAction(hTurn, hType, hTargets, hResult, hTime))
                }
            }

            val state = GameState(
                gridSize = gridSize,
                grid = grid,
                nodes = nodes,
                playerGrid = if (playerGrid.size == 64) playerGrid else List(64) { 0 },
                playerNodes = playerNodes,
                playerEnergy = playerEnergy,
                score = score,
                turns = turns,
                elapsedSeconds = elapsed,
                timeRemainingSeconds = timeRemaining,
                gameMode = mode,
                isVictory = isVictory,
                isDefeat = isDefeat,
                history = history,
                activeSkill = SkillType.NORMAL
            )

            validateGameState(state)
            Result.success(Pair(state, metadata))
        } catch (e: Exception) {
            Result.failure(Exception("Error al parsear archivo TXT: ${e.message}"))
        }
    }

    private fun parseTxtNodes(lines: List<String>): List<NetworkNode> {
        val nodes = mutableListOf<NetworkNode>()
        lines.forEach { line ->
            val p = line.split("|")
            if (p.size >= 5) {
                val nId = p[0]
                val nType = NodeType.valueOf(p[1])
                val nHits = p[2].toIntOrNull() ?: 0
                val nCompromised = p[3].toBooleanStrictOrNull() ?: false
                val nCells = p[4].split(",").filter { it.isNotBlank() }.map { it.trim().toInt() }
                nodes.add(
                    NetworkNode(
                        id = nId,
                        type = nType,
                        cells = nCells,
                        hits = nHits,
                        isCompromised = nCompromised
                    )
                )
            }
        }
        return nodes
    }

    private fun validateGameState(state: GameState) {
        if (state.grid.size != 64) {
            throw Exception("El tamaño de la cuadrícula es incorrecto (${state.grid.size} celdas en vez de 64).")
        }
        if (state.nodes.isEmpty()) {
            throw Exception("El archivo no contiene la lista de nodos requerida.")
        }
    }

    private fun extractTag(xml: String, tag: String): String? {
        val open = "<$tag>"
        val close = "</$tag>"
        val start = xml.indexOf(open)
        if (start == -1) return null
        val end = xml.indexOf(close, start + open.length)
        if (end == -1) return null
        return xml.substring(start + open.length, end).trim()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun unescapeXml(str: String): String {
        return str.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
