package com.escom.ipn.p4.cyberbreach.service

import com.escom.ipn.p4.cyberbreach.model.CyberBreachPacket
import kotlinx.coroutines.flow.StateFlow

interface IBluetoothGameService {
    val isConnected: StateFlow<Boolean>
    val isHosting: StateFlow<Boolean>
    val connectedPeerName: StateFlow<String?>
    val connectionStatusMessage: StateFlow<String>
    val incomingPacket: StateFlow<CyberBreachPacket?>
    val pairedDevices: StateFlow<List<Pair<String, String>>>

    fun refreshPairedDevices()
    fun startHosting(localName: String)
    fun connectToHost(deviceAddress: String, localName: String)
    fun sendPacket(packet: CyberBreachPacket): Boolean
    fun disconnect()
}
