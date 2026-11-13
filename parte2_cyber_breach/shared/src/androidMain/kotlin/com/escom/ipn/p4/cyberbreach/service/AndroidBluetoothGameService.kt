package com.escom.ipn.p4.cyberbreach.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.escom.ipn.p4.cyberbreach.model.CyberBreachPacket
import com.escom.ipn.p4.cyberbreach.model.GamePacketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothGameService(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : IBluetoothGameService {

    companion object {
        val GAME_UUID: UUID = UUID.fromString("e0cbf06c-cd8b-4647-bb8a-263b43f0f974")
        const val SERVICE_NAME = "CyberBreachP2P"
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val json = Json { ignoreUnknownKeys = true }

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isHosting = MutableStateFlow(false)
    override val isHosting: StateFlow<Boolean> = _isHosting.asStateFlow()

    private val _connectedPeerName = MutableStateFlow<String?>(null)
    override val connectedPeerName: StateFlow<String?> = _connectedPeerName.asStateFlow()

    private val _connectionStatusMessage = MutableStateFlow("Listo para conectar")
    override val connectionStatusMessage: StateFlow<String> = _connectionStatusMessage.asStateFlow()

    private val _incomingPacket = MutableStateFlow<CyberBreachPacket?>(null)
    override val incomingPacket: StateFlow<CyberBreachPacket?> = _incomingPacket.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    override val pairedDevices: StateFlow<List<Pair<String, String>>> = _pairedDevices.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var activeSocket: BluetoothSocket? = null
    private var writer: OutputStreamWriter? = null
    private var reader: BufferedReader? = null
    private var listenerJob: Job? = null
    private var lastReceivedSeq = -1

    init {
        refreshPairedDevices()
    }

    override fun refreshPairedDevices() {
        try {
            val bonded = bluetoothAdapter?.bondedDevices
            if (bonded != null) {
                _pairedDevices.value = bonded.map { Pair(it.name ?: "Desconocido", it.address) }
            } else {
                _pairedDevices.value = emptyList()
            }
        } catch (e: Exception) {
            _pairedDevices.value = emptyList()
        }
    }

    override fun startHosting(localName: String) {
        disconnect()
        _isHosting.value = true
        _connectionStatusMessage.value = "Esperando que se una un jugador..."
        lastReceivedSeq = -1

        scope.launch {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, GAME_UUID)
                val socket = serverSocket?.accept()
                if (socket != null) {
                    serverSocket?.close()
                    serverSocket = null
                    setupConnectedSocket(socket, isHost = true, localName = localName)
                }
            } catch (e: Exception) {
                _connectionStatusMessage.value = "Error al hospedar partida: ${e.message}"
                _isConnected.value = false
                _isHosting.value = false
            }
        }
    }

    override fun connectToHost(deviceAddress: String, localName: String) {
        disconnect()
        _isHosting.value = false
        _connectionStatusMessage.value = "Conectando al anfitrión..."
        lastReceivedSeq = -1

        scope.launch {
            try {
                val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                val socket = device?.createRfcommSocketToServiceRecord(GAME_UUID)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()
                if (socket != null && socket.isConnected) {
                    setupConnectedSocket(socket, isHost = false, localName = localName)
                } else {
                    _connectionStatusMessage.value = "No se pudo conectar al dispositivo"
                }
            } catch (e: Exception) {
                _connectionStatusMessage.value = "Fallo de conexión: ${e.message}"
                _isConnected.value = false
            }
        }
    }

    private fun setupConnectedSocket(socket: BluetoothSocket, isHost: Boolean, localName: String) {
        activeSocket = socket
        writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
        reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
        _isConnected.value = true
        _connectedPeerName.value = socket.remoteDevice?.name ?: "Jugador Remoto"
        _connectionStatusMessage.value = "¡Conectado con ${_connectedPeerName.value}!"

        // Enviar handshake inicial
        sendPacket(
            CyberBreachPacket(
                type = GamePacketType.HANDSHAKE,
                seq = 0,
                senderName = localName,
                timestamp = System.currentTimeMillis()
            )
        )

        // Escucha continua de paquetes entrantes
        listenerJob = scope.launch {
            try {
                while (_isConnected.value) {
                    val line = reader?.readLine() ?: break
                    if (line.isNotBlank()) {
                        handleIncomingJson(line)
                    }
                }
            } catch (e: Exception) {
                // Conexión interrumpida o cerrada
            } finally {
                onConnectionLost()
            }
        }
    }

    private fun handleIncomingJson(jsonStr: String) {
        try {
            val packet = json.decodeFromString<CyberBreachPacket>(jsonStr)
            // Filtro de paquetes duplicados (excepto PING o ACK)
            if (packet.seq > lastReceivedSeq || packet.type == GamePacketType.HANDSHAKE) {
                lastReceivedSeq = packet.seq
                _incomingPacket.value = packet
            }
        } catch (e: Exception) {
            // Error de deserialización
        }
    }

    override fun sendPacket(packet: CyberBreachPacket): Boolean {
        return try {
            val serialized = json.encodeToString(packet) + "\n"
            writer?.write(serialized)
            writer?.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun onConnectionLost() {
        _isConnected.value = false
        _connectionStatusMessage.value = "Conexión Bluetooth finalizada"
        cleanup()
    }

    override fun disconnect() {
        try {
            sendPacket(
                CyberBreachPacket(
                    type = GamePacketType.DISCONNECT,
                    seq = lastReceivedSeq + 1,
                    senderName = "Player",
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (ignored: Exception) {}

        cleanup()
        _isConnected.value = false
        _isHosting.value = false
        _connectionStatusMessage.value = "Desconectado"
    }

    private fun cleanup() {
        listenerJob?.cancel()
        listenerJob = null
        try { writer?.close() } catch (ignored: Exception) {}
        try { reader?.close() } catch (ignored: Exception) {}
        try { activeSocket?.close() } catch (ignored: Exception) {}
        try { serverSocket?.close() } catch (ignored: Exception) {}
        writer = null
        reader = null
        activeSocket = null
        serverSocket = null
    }
}
