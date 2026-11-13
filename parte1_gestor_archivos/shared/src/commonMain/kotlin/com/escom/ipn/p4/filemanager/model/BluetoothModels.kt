package com.escom.ipn.p4.filemanager.model

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothPeerDevice(
    val address: String,
    val name: String,
    val isBonded: Boolean = false,
    val isConnected: Boolean = false
)

enum class TransferDirection {
    OUTGOING, // Enviado
    INCOMING  // Recibido
}

enum class TransferStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}

@Serializable
data class FileTransferRecord(
    val id: String,
    val fileName: String,
    val fileExtension: String,
    val totalBytes: Long,
    val transferredBytes: Long = 0L,
    val direction: TransferDirection,
    val status: TransferStatus,
    val sha256Expected: String = "",
    val sha256Calculated: String = "",
    val peerName: String,
    val peerAddress: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (totalBytes > 0L) (transferredBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
    
    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()
        
    val isIntegrityValid: Boolean
        get() = sha256Expected.isNotBlank() && sha256Expected.equals(sha256Calculated, ignoreCase = true)
}

@Serializable
data class FileTransferMetadataPacket(
    val transferId: String,
    val fileName: String,
    val fileExtension: String,
    val mimeType: String,
    val totalBytes: Long,
    val sha256: String,
    val senderName: String
)

@Serializable
data class FileChunkPacket(
    val transferId: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val payloadBase64: String
)

@Serializable
data class TransferAckPacket(
    val transferId: String,
    val chunkIndex: Int,
    val isSuccess: Boolean
)
