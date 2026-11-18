package com.escom.ipn.p4.cyberbreach.model

import kotlinx.serialization.Serializable

enum class GameMode(val displayName: String, val description: String) {
    INFILTRATION("Infiltración en Solitario", "Turnos libres para descubrir y neutralizar los nodos enemigos."),
    CPU_DUEL("Cyber Duelo vs CPU", "Combate 1v1 por turnos: atacas a la red enemiga y la CPU contraataca tu infraestructura."),
    BLUETOOTH_P2P("Ciberduelo Bluetooth 1v1", "Combate multijugador local sincronizado entre dos dispositivos.")
}

enum class SaveFormat(val extension: String) {
    TXT(".txt"),
    XML(".xml"),
    JSON(".json")
}

enum class NodeType(val displayName: String, val size: Int, val iconName: String) {
    FIREWALL("Firewall Gateway", 1, "security"),
    DATABASE("Base de Datos SQL", 2, "storage"),
    WEB_SERVER("Servidor Web Nginx", 3, "dns"),
    MAINFRAME("Mainframe Central", 4, "memory")
}

@Serializable
data class NetworkNode(
    val id: String,
    val type: NodeType,
    val cells: List<Int>,
    val hits: Int = 0,
    val isCompromised: Boolean = false
) {
    val totalSize: Int get() = type.size
    val remainingHealth: Int get() = (totalSize - hits).coerceAtLeast(0)
}

@Serializable
data class GameAction(
    val turn: Int,
    val actionType: String, // STRIKE, PING_SWEEP, DDOS, CPU_COUNTER
    val targetIndices: List<Int>,
    val result: String, // HIT, MISS, DESTROYED, PING_DETECTED, PING_CLEAR
    val timestamp: String
)

@Serializable
data class SaveMetadata(
    val id: String,
    val title: String,
    val categoryTag: String = "Campaña", // "Campaña", "Práctica", "Difícil", "Récord", "Favorito", "Duelo CPU"
    val format: SaveFormat,
    val savedAt: String,
    val score: Int,
    val mode: GameMode,
    val turns: Int,
    val elapsedSeconds: Int,
    val isFinished: Boolean,
    val fileName: String
)

enum class SkillType(val displayName: String, val energyCost: Int, val description: String) {
    NORMAL("Ataque Simple", 0, "Disparo estándar a 1 casilla"),
    PING_SWEEP("Ping Sweep", 20, "Escanea área de 2x2 detectando presencia de nodos"),
    DDOS("DDoS Attack", 30, "Impacta 3 casillas consecutivas en línea")
}

const val CELL_HIDDEN = 0
const val CELL_MISS = 1
const val CELL_HIT = 2
const val CELL_DESTROYED = 3

@Serializable
data class GameState(
    val gridSize: Int = 8,
    val grid: List<Int> = List(64) { CELL_HIDDEN },
    val nodes: List<NetworkNode> = emptyList(),
    val playerGrid: List<Int> = List(64) { CELL_HIDDEN },
    val playerNodes: List<NetworkNode> = emptyList(),
    val cpuLastAction: String? = null,
    val playerEnergy: Int = 0,
    val score: Int = 0,
    val turns: Int = 0,
    val elapsedSeconds: Int = 0,
    val timeRemainingSeconds: Int = 90, // para Contrarreloj (si aplica)
    val gameMode: GameMode = GameMode.INFILTRATION,
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false,
    val history: List<GameAction> = emptyList(),
    val activeSkill: SkillType = SkillType.NORMAL,
    // Práctica 6: Multijugador Bluetooth y Sensores
    val isMyTurn: Boolean = true,
    val isBluetoothSession: Boolean = false,
    val isHost: Boolean = true,
    val opponentName: String = "Contrincante",
    val targetedCellBySensor: Int? = null,
    val sensorTiltActive: Boolean = false,
    val kineticCooldownSeconds: Int = 0,
    val bluetoothStatusMessage: String? = null
) {
    val totalNodes: Int get() = nodes.size
    val compromisedNodes: Int get() = nodes.count { it.isCompromised }
    val totalPlayerNodes: Int get() = playerNodes.size
    val compromisedPlayerNodes: Int get() = playerNodes.count { it.isCompromised }
    val progressPercentage: Int
        get() {
            val totalCells = nodes.sumOf { it.totalSize }
            if (totalCells == 0) return 0
            val hitCells = nodes.sumOf { it.hits }
            return ((hitCells.toDouble() / totalCells.toDouble()) * 100).toInt().coerceIn(0, 100)
        }
    val isGameOver: Boolean get() = isVictory || isDefeat
}

enum class CyberTheme {
    GUINDA_IPN,
    AZUL_ESCOM
}

@Serializable
enum class GamePacketType {
    HANDSHAKE,
    HANDSHAKE_ACK,
    ATTACK,
    ATTACK_RESULT,
    SURRENDER,
    DISCONNECT
}

@Serializable
data class CyberBreachPacket(
    val type: GamePacketType,
    val seq: Int,
    val senderName: String,
    val targetCell: Int = -1,
    val skill: SkillType = SkillType.NORMAL,
    val hitResult: String? = null, // "HIT", "MISS", "DESTROYED"
    val destroyedNodeName: String? = null,
    val isGameOver: Boolean = false,
    val didAttackerWin: Boolean = false,
    val timestamp: Long = 0L
)

interface IGameSensorListener {
    fun onTiltAiming(targetCell: Int, pitch: Float, roll: Float)
    fun onKineticShake()
}

interface IGameSensorManager {
    fun startListening(listener: IGameSensorListener)
    fun stopListening()
    fun isAvailable(): Boolean
}
