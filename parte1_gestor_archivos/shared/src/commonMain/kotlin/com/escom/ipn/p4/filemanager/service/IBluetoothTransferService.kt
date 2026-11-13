package com.escom.ipn.p4.filemanager.service

import com.escom.ipn.p4.filemanager.model.BluetoothPeerDevice
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.FileTransferRecord
import com.escom.ipn.p4.filemanager.model.HttpProxyResponse
import com.escom.ipn.p4.filemanager.model.WebCacheRecord
import kotlinx.coroutines.flow.StateFlow

interface IBluetoothTransferService {
    val bondedDevicesFlow: StateFlow<List<BluetoothPeerDevice>>
    val isBluetoothEnabled: Boolean
    val isServerRunningFlow: StateFlow<Boolean>
    val connectedPeerFlow: StateFlow<BluetoothPeerDevice?>
    val transferHistoryFlow: StateFlow<List<FileTransferRecord>>
    val activeTransferFlow: StateFlow<FileTransferRecord?>
    val pendingIncomingOfferFlow: StateFlow<FileTransferMetadataPacket?>
    val proxyResponseFlow: StateFlow<HttpProxyResponse?>
    val webCacheFlow: StateFlow<List<WebCacheRecord>>

    fun refreshDevices()
    fun startServer()
    fun stopServer()
    fun connectToDevice(device: BluetoothPeerDevice)
    fun disconnect()

    suspend fun sendFile(
        device: BluetoothPeerDevice,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<FileTransferRecord>

    suspend fun acceptIncomingTransfer(): Result<Unit>
    fun rejectIncomingTransfer()
    fun cancelActiveTransfer()

    suspend fun sendProxyRequest(url: String): HttpProxyResponse
    fun clearCache()
    
    // Método para simulación de transferencias y pruebas locales
    fun simulateTransfer(fileName: String, mimeType: String, bytes: ByteArray, asSender: Boolean)
    fun injectIncomingOffer(offer: FileTransferMetadataPacket)
}
