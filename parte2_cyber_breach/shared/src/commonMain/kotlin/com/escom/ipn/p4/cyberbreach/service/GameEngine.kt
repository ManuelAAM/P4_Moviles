package com.escom.ipn.p4.cyberbreach.service

import com.escom.ipn.p4.cyberbreach.model.CELL_DESTROYED
import com.escom.ipn.p4.cyberbreach.model.CELL_HIDDEN
import com.escom.ipn.p4.cyberbreach.model.CELL_HIT
import com.escom.ipn.p4.cyberbreach.model.CELL_MISS
import com.escom.ipn.p4.cyberbreach.model.GameAction
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.NetworkNode
import com.escom.ipn.p4.cyberbreach.model.NodeType
import com.escom.ipn.p4.cyberbreach.model.SkillType
import kotlin.random.Random

object GameEngine {

    const val GRID_SIZE = 8
    const val TOTAL_CELLS = 64

    fun startNewGame(mode: GameMode = GameMode.INFILTRATION): GameState {
        val enemyNodes = generateRandomNodes()
        val playerNodes = if (mode == GameMode.CPU_DUEL) generateRandomNodes() else emptyList()

        return GameState(
            gridSize = GRID_SIZE,
            grid = List(TOTAL_CELLS) { CELL_HIDDEN },
            nodes = enemyNodes,
            playerGrid = List(TOTAL_CELLS) { CELL_HIDDEN },
            playerNodes = playerNodes,
            cpuLastAction = null,
            playerEnergy = 0,
            score = 0,
            turns = 0,
            elapsedSeconds = 0,
            timeRemainingSeconds = 90,
            gameMode = mode,
            isVictory = false,
            isDefeat = false,
            history = emptyList(),
            activeSkill = SkillType.NORMAL
        )
    }

    private fun generateRandomNodes(): List<NetworkNode> {
        val occupied = mutableSetOf<Int>()
        val result = mutableListOf<NetworkNode>()

        val nodeDefinitions = listOf(
            Pair(NodeType.MAINFRAME, 4),
            Pair(NodeType.WEB_SERVER, 3),
            Pair(NodeType.DATABASE, 2),
            Pair(NodeType.FIREWALL, 1)
        )

        for ((type, size) in nodeDefinitions) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 200) {
                attempts++
                val horizontal = Random.nextBoolean()
                val r = if (horizontal) Random.nextInt(GRID_SIZE) else Random.nextInt(GRID_SIZE - size + 1)
                val c = if (horizontal) Random.nextInt(GRID_SIZE - size + 1) else Random.nextInt(GRID_SIZE)

                val cells = mutableListOf<Int>()
                var collision = false
                for (i in 0 until size) {
                    val cell = if (horizontal) r * GRID_SIZE + (c + i) else (r + i) * GRID_SIZE + c
                    if (occupied.contains(cell)) {
                        collision = true
                        break
                    }
                    cells.add(cell)
                }

                if (!collision) {
                    occupied.addAll(cells)
                    result.add(
                        NetworkNode(
                            id = "node_${type.name.lowercase()}_${Random.nextInt(1000, 9999)}",
                            type = type,
                            cells = cells,
                            hits = 0,
                            isCompromised = false
                        )
                    )
                    placed = true
                }
            }
        }
        return result
    }

    fun executeTurn(currentState: GameState, targetCell: Int, timestamp: String): Pair<GameState, String> {
        if (currentState.isGameOver) return Pair(currentState, "Partida terminada")
        if (targetCell < 0 || targetCell >= TOTAL_CELLS) return Pair(currentState, "Casilla inválida")

        val (playerState, playerFeedback) = when (currentState.activeSkill) {
            SkillType.NORMAL -> strikeSingle(currentState, targetCell, timestamp)
            SkillType.PING_SWEEP -> executePingSweep(currentState, targetCell, timestamp)
            SkillType.DDOS -> executeDDoS(currentState, targetCell, timestamp)
        }

        // Si el jugador no terminó la partida y el modo es CPU_DUEL, la CPU contraataca
        if (!playerState.isGameOver && playerState.gameMode == GameMode.CPU_DUEL && playerState.playerNodes.isNotEmpty()) {
            val (duelState, cpuFeedback) = executeCpuCounterAttack(playerState, timestamp)
            return Pair(duelState, "$playerFeedback | $cpuFeedback")
        }

        return Pair(playerState, playerFeedback)
    }

    private fun strikeSingle(currentState: GameState, targetCell: Int, timestamp: String): Pair<GameState, String> {
        val currentGrid = currentState.grid.toMutableList()
        val currentCellState = currentGrid[targetCell]

        if (currentCellState != CELL_HIDDEN) {
            return Pair(currentState, "Casilla ya explorada previamente")
        }

        val targetNode = currentState.nodes.find { it.cells.contains(targetCell) }
        val newTurns = currentState.turns + 1

        if (targetNode != null) {
            // Impacto exitoso
            val newHits = targetNode.hits + 1
            val isNowCompromised = newHits >= targetNode.totalSize

            val updatedNodes = currentState.nodes.map { node ->
                if (node.id == targetNode.id) {
                    node.copy(hits = newHits, isCompromised = isNowCompromised)
                } else node
            }

            // Actualizar grid
            if (isNowCompromised) {
                targetNode.cells.forEach { c -> currentGrid[c] = CELL_DESTROYED }
            } else {
                currentGrid[targetCell] = CELL_HIT
            }

            // Recompensa de energía y puntuación
            val addedScore = if (isNowCompromised) 500 else 100
            val newScore = currentState.score + addedScore
            val newEnergy = (currentState.playerEnergy + 10).coerceAtMost(100)

            val allCompromised = updatedNodes.all { it.isCompromised }

            val actionResult = if (isNowCompromised) "NODO_DESTRUIDO: ${targetNode.type.displayName}" else "IMPACTO_DETECTADO"
            val newAction = GameAction(
                turn = newTurns,
                actionType = "STRIKE",
                targetIndices = listOf(targetCell),
                result = actionResult,
                timestamp = timestamp
            )

            val nextState = currentState.copy(
                grid = currentGrid,
                nodes = updatedNodes,
                playerEnergy = newEnergy,
                score = newScore,
                turns = newTurns,
                isVictory = allCompromised,
                history = currentState.history + newAction,
                activeSkill = SkillType.NORMAL
            )

            val feedback = if (isNowCompromised) "¡Nodo ${targetNode.type.displayName} destruido!" else "¡Impacto en nodo enemigo! +10 EN"
            return Pair(nextState, feedback)
        } else {
            // Fallo
            currentGrid[targetCell] = CELL_MISS
            val newAction = GameAction(
                turn = newTurns,
                actionType = "STRIKE",
                targetIndices = listOf(targetCell),
                result = "FALLO_VACÍO",
                timestamp = timestamp
            )

            val nextState = currentState.copy(
                grid = currentGrid,
                turns = newTurns,
                history = currentState.history + newAction,
                activeSkill = SkillType.NORMAL
            )
            return Pair(nextState, "Fallo. Ningún nodo en este sector.")
        }
    }

    private fun executeCpuCounterAttack(currentState: GameState, timestamp: String): Pair<GameState, String> {
        val playerGrid = currentState.playerGrid.toMutableList()
        val availableCells = playerGrid.indices.filter { playerGrid[it] == CELL_HIDDEN }

        if (availableCells.isEmpty()) return Pair(currentState, "")

        // Inteligencia simple de la CPU: si hay un nodo tocado pero no destruido, buscar alrededor
        val woundedNode = currentState.playerNodes.find { it.hits > 0 && !it.isCompromised }
        val cpuTargetCell = if (woundedNode != null && Random.nextFloat() < 0.65f) {
            // Intentar buscar celdas adyacentes a un impacto previo
            val hitCell = woundedNode.cells.find { playerGrid[it] == CELL_HIT }
            val adjacentCandidates = if (hitCell != null) {
                val r = hitCell / GRID_SIZE
                val c = hitCell % GRID_SIZE
                listOf(
                    Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1)
                ).filter { (ar, ac) -> ar in 0 until GRID_SIZE && ac in 0 until GRID_SIZE }
                    .map { (ar, ac) -> ar * GRID_SIZE + ac }
                    .filter { playerGrid[it] == CELL_HIDDEN }
            } else emptyList()

            if (adjacentCandidates.isNotEmpty()) adjacentCandidates.random() else availableCells.random()
        } else {
            availableCells.random()
        }

        val hitNode = currentState.playerNodes.find { it.cells.contains(cpuTargetCell) }
        val colLetter = ('A'.code + (cpuTargetCell % GRID_SIZE)).toChar()
        val rowNum = (cpuTargetCell / GRID_SIZE) + 1
        val coordName = "$colLetter$rowNum"

        if (hitNode != null) {
            val newHits = hitNode.hits + 1
            val isNowCompromised = newHits >= hitNode.totalSize

            val updatedPlayerNodes = currentState.playerNodes.map { node ->
                if (node.id == hitNode.id) node.copy(hits = newHits, isCompromised = isNowCompromised) else node
            }

            if (isNowCompromised) {
                hitNode.cells.forEach { c -> playerGrid[c] = CELL_DESTROYED }
            } else {
                playerGrid[cpuTargetCell] = CELL_HIT
            }

            val allPlayerNodesCompromised = updatedPlayerNodes.all { it.isCompromised }

            val cpuMsg = if (isNowCompromised) {
                "CPU neutralizó tu [${hitNode.type.displayName}] en $coordName"
            } else {
                "CPU impactó tu red en $coordName"
            }

            val cpuAction = GameAction(
                turn = currentState.turns,
                actionType = "CPU_COUNTER",
                targetIndices = listOf(cpuTargetCell),
                result = cpuMsg,
                timestamp = timestamp
            )

            val updatedState = currentState.copy(
                playerGrid = playerGrid,
                playerNodes = updatedPlayerNodes,
                cpuLastAction = cpuMsg,
                isDefeat = allPlayerNodesCompromised,
                history = currentState.history + cpuAction
            )

            return Pair(updatedState, cpuMsg)
        } else {
            playerGrid[cpuTargetCell] = CELL_MISS
            val cpuMsg = "CPU disparó en $coordName y falló"

            val cpuAction = GameAction(
                turn = currentState.turns,
                actionType = "CPU_COUNTER",
                targetIndices = listOf(cpuTargetCell),
                result = "CPU_FALLO en $coordName",
                timestamp = timestamp
            )

            val updatedState = currentState.copy(
                playerGrid = playerGrid,
                cpuLastAction = cpuMsg,
                history = currentState.history + cpuAction
            )

            return Pair(updatedState, cpuMsg)
        }
    }

    private fun executePingSweep(currentState: GameState, targetCell: Int, timestamp: String): Pair<GameState, String> {
        if (currentState.playerEnergy < SkillType.PING_SWEEP.energyCost) {
            return Pair(currentState, "Energía insuficiente (${currentState.playerEnergy}/${SkillType.PING_SWEEP.energyCost})")
        }

        val row = targetCell / GRID_SIZE
        val col = targetCell % GRID_SIZE

        val scanCells = mutableListOf<Int>()
        for (dr in 0..1) {
            for (dc in 0..1) {
                val r = row + dr
                val c = col + dc
                if (r < GRID_SIZE && c < GRID_SIZE) {
                    scanCells.add(r * GRID_SIZE + c)
                }
            }
        }

        val nodesInSector = currentState.nodes.filter { node ->
            !node.isCompromised && node.cells.any { scanCells.contains(it) }
        }

        val remainingEnergy = currentState.playerEnergy - SkillType.PING_SWEEP.energyCost
        val newTurns = currentState.turns + 1

        val resultMsg = if (nodesInSector.isNotEmpty()) {
            "PING_DETECTADO: ${nodesInSector.size} firma(s) en sector 2x2"
        } else {
            "PING_LIMPIO: Sin actividad en sector 2x2"
        }

        val newAction = GameAction(
            turn = newTurns,
            actionType = "PING_SWEEP",
            targetIndices = scanCells,
            result = resultMsg,
            timestamp = timestamp
        )

        val nextState = currentState.copy(
            playerEnergy = remainingEnergy,
            turns = newTurns,
            history = currentState.history + newAction,
            activeSkill = SkillType.NORMAL
        )

        val feedback = if (nodesInSector.isNotEmpty()) {
            "¡Radar Ping Sweep detectó ${nodesInSector.size} nodo(s) en el cuadrante!"
        } else {
            "Ping Sweep: Sector completamente despejado."
        }
        return Pair(nextState, feedback)
    }

    private fun executeDDoS(currentState: GameState, targetCell: Int, timestamp: String): Pair<GameState, String> {
        if (currentState.playerEnergy < SkillType.DDOS.energyCost) {
            return Pair(currentState, "Energía insuficiente (${currentState.playerEnergy}/${SkillType.DDOS.energyCost})")
        }

        val row = targetCell / GRID_SIZE
        val col = targetCell % GRID_SIZE

        val targets = mutableListOf<Int>()
        if (col + 2 < GRID_SIZE) {
            targets.add(row * GRID_SIZE + col)
            targets.add(row * GRID_SIZE + col + 1)
            targets.add(row * GRID_SIZE + col + 2)
        } else {
            val rStart = (row - 1).coerceAtLeast(0)
            for (r in rStart until (rStart + 3).coerceAtMost(GRID_SIZE)) {
                targets.add(r * GRID_SIZE + col)
            }
        }

        var workingState = currentState.copy(
            playerEnergy = currentState.playerEnergy - SkillType.DDOS.energyCost,
            activeSkill = SkillType.NORMAL
        )

        var hitCount = 0
        targets.forEach { cell ->
            val (st, _) = strikeSingle(workingState, cell, timestamp)
            if (st.score > workingState.score) hitCount++
            workingState = st
        }

        val newAction = GameAction(
            turn = workingState.turns,
            actionType = "DDOS",
            targetIndices = targets,
            result = "DDOS_IMPACTOS: $hitCount",
            timestamp = timestamp
        )

        val nextState = workingState.copy(
            history = workingState.history + newAction
        )

        return Pair(nextState, "¡Ataque DDoS ejecutado sobre 3 casillas! Impactos: $hitCount")
    }
}
