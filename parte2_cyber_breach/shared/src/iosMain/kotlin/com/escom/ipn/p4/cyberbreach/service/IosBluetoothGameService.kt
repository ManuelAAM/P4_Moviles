package com.escom.ipn.p4.cyberbreach.service

import com.escom.ipn.p4.cyberbreach.model.CyberBreachPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosBluetoothGameService : IBluetoothGameService {
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override val isHosting: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    override val connectedPeerName: StateFlow<String?> = MutableStateFlow<String?>(null).asStateFlow()
    override val connectionStatusMessage: StateFlow<String> = MutableStateFlow("iOS Bluetooth no disponible en simulador").asStateFlow()
    override val incomingPacket: StateFlow<CyberBreachPacket?> = MutableStateFlow<CyberBreachPacket?>(null).asStateFlow()
    override val pairedDevices: StateFlow<List<Pair<String, String>>> = MutableStateFlow(emptyList<Pair<String, String>>()).asStateFlow()

    override fun refreshPairedDevices() {}
    override fun startHosting(localName: String) {}
    override fun connectToHost(deviceAddress: String, localName: String) {}
    override fun sendPacket(packet: CyberBreachPacket): Boolean = false
    override fun disconnect() {}
}
