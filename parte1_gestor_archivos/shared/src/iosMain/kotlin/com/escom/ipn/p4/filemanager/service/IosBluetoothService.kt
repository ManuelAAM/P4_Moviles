package com.escom.ipn.p4.filemanager.service

import com.escom.ipn.p4.filemanager.model.BluetoothPeerDevice
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.FileTransferRecord
import com.escom.ipn.p4.filemanager.model.HttpProxyResponse
import com.escom.ipn.p4.filemanager.model.TransferDirection
import com.escom.ipn.p4.filemanager.model.TransferStatus
import com.escom.ipn.p4.filemanager.model.WebCacheRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosBluetoothService : IBluetoothTransferService {
    private val _bondedDevicesFlow = MutableStateFlow(
        listOf(BluetoothPeerDevice("ios_peer_1", "iPhone 15 Pro (BLE)", isBonded = true))
    )
    override val bondedDevicesFlow: StateFlow<List<BluetoothPeerDevice>> = _bondedDevicesFlow.asStateFlow()

    override val isBluetoothEnabled: Boolean = true
    override val isServerRunningFlow: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override val connectedPeerFlow: StateFlow<BluetoothPeerDevice?> = MutableStateFlow(null).asStateFlow()
    override val transferHistoryFlow: StateFlow<List<FileTransferRecord>> = MutableStateFlow(emptyList<FileTransferRecord>()).asStateFlow()
    override val activeTransferFlow: StateFlow<FileTransferRecord?> = MutableStateFlow(null).asStateFlow()
    override val pendingIncomingOfferFlow: StateFlow<FileTransferMetadataPacket?> = MutableStateFlow(null).asStateFlow()
    override val proxyResponseFlow: StateFlow<HttpProxyResponse?> = MutableStateFlow(null).asStateFlow()
    override val webCacheFlow: StateFlow<List<WebCacheRecord>> = MutableStateFlow(emptyList<WebCacheRecord>()).asStateFlow()

    override fun refreshDevices() {}
    override fun startServer() {}
    override fun stopServer() {}
    override fun connectToDevice(device: BluetoothPeerDevice) {}
    override fun disconnect() {}

    override suspend fun sendFile(
        device: BluetoothPeerDevice,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<FileTransferRecord> {
        val record = FileTransferRecord(
            id = "ios_tx_1",
            fileName = fileName,
            fileExtension = fileName.substringAfterLast(".", ""),
            totalBytes = bytes.size.toLong(),
            transferredBytes = bytes.size.toLong(),
            direction = TransferDirection.OUTGOING,
            status = TransferStatus.COMPLETED,
            sha256Expected = Sha256Util.hash(bytes),
            sha256Calculated = Sha256Util.hash(bytes),
            peerName = device.name,
            peerAddress = device.address,
            startedAt = 0L
        )
        return Result.success(record)
    }

    override suspend fun acceptIncomingTransfer(): Result<Unit> = Result.success(Unit)
    override fun rejectIncomingTransfer() {}
    override fun cancelActiveTransfer() {}
    override suspend fun sendProxyRequest(url: String): HttpProxyResponse {
        return HttpProxyResponse("ios_req", url, 200, "{\"msg\":\"iOS Proxy Response\"}", fromCache = true)
    }
    override fun clearCache() {}
    override fun simulateTransfer(fileName: String, mimeType: String, bytes: ByteArray, asSender: Boolean) {}
    override fun injectIncomingOffer(offer: FileTransferMetadataPacket) {}
}
