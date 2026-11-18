package com.escom.ipn.p4.cyberbreach.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val json = Json { ignoreUnknownKeys = true }
    private val writeLock = Any()

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

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun refreshPairedDevices() {
        if (!hasBluetoothPermissions()) {
            _pairedDevices.value = emptyList()
            _connectionStatusMessage.value = "Falta permiso de Dispositivos Cercanos (Bluetooth). Pulsa 'Conceder Permisos'."
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _pairedDevices.value = emptyList()
            _connectionStatusMessage.value = "Este dispositivo no tiene soporte Bluetooth."
            return
        }
        if (!adapter.isEnabled) {
            _pairedDevices.value = emptyList()
            _connectionStatusMessage.value = "Bluetooth está apagado. Enciéndelo en Ajustes."
            return
        }
        try {
            val bonded = adapter.bondedDevices
            if (bonded != null) {
                _pairedDevices.value = bonded.map { Pair(it.name ?: "Dispositivo Sin Nombre", it.address) }
                if (!_isHosting.value && !_isConnected.value) {
                    _connectionStatusMessage.value = if (bonded.isEmpty()) {
                        "Sin dispositivos emparejados. Vincúlalo primero en Ajustes Bluetooth."
                    } else {
                        "Listo (${bonded.size} vinculados). Elige Hospedar o Unirse."
                    }
                }
            } else {
                _pairedDevices.value = emptyList()
            }
        } catch (se: SecurityException) {
            _pairedDevices.value = emptyList()
            _connectionStatusMessage.value = "Permiso Bluetooth denegado (${se.message})."
        } catch (e: Exception) {
            _pairedDevices.value = emptyList()
        }
    }

    override fun startHosting(localName: String) {
        if (!hasBluetoothPermissions()) {
            _isHosting.value = false
            _connectionStatusMessage.value = "Permiso de Bluetooth requerido. Pulsa 'Conceder Permisos' arriba."
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _isHosting.value = false
            _connectionStatusMessage.value = "El Bluetooth está apagado. Enciéndelo primero."
            return
        }

        _isHosting.value = true
        _connectionStatusMessage.value = "Esperando que se una un jugador en CyberBreachP2P..."
        lastReceivedSeq = -1

        scope.launch {
            cleanup()
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, GAME_UUID)
                val socket = serverSocket?.accept()
                if (socket != null) {
                    try { serverSocket?.close() } catch (_: Exception) {}
                    serverSocket = null
                    setupConnectedSocket(socket, isHost = true, localName = localName)
                }
            } catch (se: SecurityException) {
                _connectionStatusMessage.value = "Falta permiso BLUETOOTH_CONNECT: ${se.message}"
                _isConnected.value = false
                _isHosting.value = false
            } catch (e: Exception) {
                // Evitar mostrar error si el usuario canceló deliberadamente el servidor
                if (_isHosting.value) {
                    _connectionStatusMessage.value = "Error al hospedar partida: ${e.message}"
                }
                _isConnected.value = false
                _isHosting.value = false
            }
        }
    }

    override fun connectToHost(deviceAddress: String, localName: String) {
        if (!hasBluetoothPermissions()) {
            _isHosting.value = false
            _connectionStatusMessage.value = "Permiso de Bluetooth requerido. Pulsa 'Conceder Permisos'."
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _isHosting.value = false
            _connectionStatusMessage.value = "El Bluetooth está apagado. Enciéndelo primero."
            return
        }

        _isHosting.value = false
        _connectionStatusMessage.value = "Conectando al anfitrión ($deviceAddress)..."
        lastReceivedSeq = -1

        scope.launch {
            cleanup()
            try {
                val device: BluetoothDevice? = adapter.getRemoteDevice(deviceAddress)
                val socket = device?.createRfcommSocketToServiceRecord(GAME_UUID)
                try { adapter.cancelDiscovery() } catch (_: Exception) {}
                socket?.connect()
                if (socket != null && socket.isConnected) {
                    setupConnectedSocket(socket, isHost = false, localName = localName)
                } else {
                    _connectionStatusMessage.value = "No se pudo conectar al dispositivo"
                }
            } catch (se: SecurityException) {
                _connectionStatusMessage.value = "Falta permiso BLUETOOTH_CONNECT: ${se.message}"
                _isConnected.value = false
            } catch (e: Exception) {
                _connectionStatusMessage.value = "Fallo de conexión: asegúrate de que el otro teléfono haya pulsado '1. HOSPEDAR' primero. (${e.message})"
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
        val currentWriter = writer ?: return false
        val serialized = try {
            json.encodeToString(packet) + "\n"
        } catch (e: Exception) {
            return false
        }
        scope.launch {
            synchronized(writeLock) {
                try {
                    currentWriter.write(serialized)
                    currentWriter.flush()
                } catch (_: Exception) {
                }
            }
        }
        return activeSocket?.isConnected == true
    }

    private fun onConnectionLost() {
        _isConnected.value = false
        _isHosting.value = false
        _connectionStatusMessage.value = "Conexión Bluetooth finalizada"
        cleanup()
    }

    override fun disconnect() {
        _isConnected.value = false
        _isHosting.value = false
        _connectionStatusMessage.value = "Desconectado"
        val currentWriter = writer
        scope.launch {
            if (currentWriter != null) {
                synchronized(writeLock) {
                    try {
                        val pkt = CyberBreachPacket(
                            type = GamePacketType.DISCONNECT,
                            seq = lastReceivedSeq + 1,
                            senderName = "Player",
                            timestamp = System.currentTimeMillis()
                        )
                        currentWriter.write(json.encodeToString(pkt) + "\n")
                        currentWriter.flush()
                    } catch (_: Exception) {}
                }
            }
            cleanup()
        }
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
