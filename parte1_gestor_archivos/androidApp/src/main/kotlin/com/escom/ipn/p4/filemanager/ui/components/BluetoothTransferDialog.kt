package com.escom.ipn.p4.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.filemanager.model.BluetoothPeerDevice
import com.escom.ipn.p4.filemanager.model.FileCategory
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.FileTransferRecord
import com.escom.ipn.p4.filemanager.model.TransferDirection
import com.escom.ipn.p4.filemanager.model.TransferStatus

@Composable
fun BluetoothTransferDialog(
    selectedFile: FileItem?,
    availableFiles: List<FileItem> = emptyList(),
    devices: List<BluetoothPeerDevice>,
    connectedPeer: BluetoothPeerDevice?,
    activeTransfer: FileTransferRecord?,
    transferHistory: List<FileTransferRecord>,
    pendingOffer: FileTransferMetadataPacket?,
    onConnectDevice: (BluetoothPeerDevice) -> Unit,
    onSendFile: (FileItem, BluetoothPeerDevice) -> Unit,
    onSimulateTransfer: (FileItem) -> Unit,
    onShareViaSystemBluetooth: (FileItem) -> Unit = {},
    onSimulateIncomingOffer: () -> Unit,
    onAcceptIncoming: () -> Unit,
    onRejectIncoming: () -> Unit,
    onCancelTransfer: () -> Unit,
    onDismiss: () -> Unit
) {
    // Archivo predeterminado si no se abrió desde un archivo específico
    val defaultFallbackFile = remember {
        FileItem(
            name = "imagen_ejemplo_petergriffin.jpg",
            path = "Documentos/imagen_ejemplo_petergriffin.jpg",
            isDirectory = false,
            sizeBytes = 25702L,
            formattedSize = "25.1 KB",
            lastModifiedEpochMs = System.currentTimeMillis(),
            formattedDate = "Hoy",
            extension = "jpg",
            mimeType = "image/jpeg",
            category = FileCategory.IMAGE
        )
    }

    // Archivo activo para envío (permite cambiarlo dinámicamente)
    var activeFileToSend by remember(selectedFile) {
        mutableStateOf(selectedFile ?: availableFiles.firstOrNull { !it.isDirectory } ?: defaultFallbackFile)
    }

    var selectedTabIndex by remember { mutableIntStateOf(if (pendingOffer != null) 1 else 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Transferencia Bluetooth", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Ejercicio 2 • RFCOMM / SHA-256", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Estado del enlace Bluetooth
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (connectedPeer != null) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (connectedPeer != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (connectedPeer != null) "Conectado con: ${connectedPeer.name}" else "Enlace: Servidor RFCOMM activo (Listo)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Límite: 5 MB • Bloques 2 KB • Checksum SHA-256",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ALERTA DE OFERTA ENTRANTE (Requisito de aceptación explícita de Ejercicio 2)
                if (pendingOffer != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CallReceived, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Solicitud de Transferencia Entrante",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Archivo: ${pendingOffer.fileName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                            Text("• Tamaño: ${pendingOffer.totalBytes} bytes", fontSize = 10.sp, color = Color(0xFF4B5563))
                            Text("• Emisor: ${pendingOffer.senderName}", fontSize = 10.sp, color = Color(0xFF4B5563))
                            Text("• SHA-256: ${pendingOffer.sha256.take(24)}...", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF047857))

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(
                                    onClick = onRejectIncoming,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Rechazar", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onAcceptIncoming,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Aceptar y Recibir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // TRANSFERENCIA ACTIVA EN CURSO
                if (activeTransfer != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (activeTransfer.direction == TransferDirection.OUTGOING) Icons.Default.FileUpload else Icons.Default.CallReceived,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${if (activeTransfer.direction == TransferDirection.OUTGOING) "Enviando" else "Recibiendo"}: ${activeTransfer.fileName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text("${activeTransfer.progressPercentage}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { activeTransfer.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${activeTransfer.transferredBytes} / ${activeTransfer.totalBytes} bytes", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                TextButton(
                                    onClick = onCancelTransfer,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Cancelar", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Pestañas de Navegación del Diálogo: Enviar / Recibir / Historial
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabPill(
                        title = "📤 Enviar",
                        isSelected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        title = "📥 Recibir",
                        isSelected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        title = "📜 Historial (${transferHistory.size})",
                        isSelected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTabIndex) {
                    0 -> {
                        // SECCIÓN ENVIAR ARCHIVO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Archivo a transferir:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeFileToSend.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Tamaño: ${activeFileToSend.formattedSize} (${activeFileToSend.sizeBytes} B) • MIME: ${activeFileToSend.mimeType}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Elige un archivo a transferir:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Chips de archivos rápidos con etiquetas cortas y no truncadas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    QuickFileChip(
                                        label = "Imagen",
                                        isSelected = activeFileToSend.name.contains("peter", ignoreCase = true),
                                        onClick = {
                                            activeFileToSend = availableFiles.firstOrNull { it.name.contains("peter", ignoreCase = true) } ?: defaultFallbackFile
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuickFileChip(
                                        label = "Archivo 1",
                                        isSelected = activeFileToSend.name.contains("bienvenida", ignoreCase = true),
                                        onClick = {
                                            activeFileToSend = availableFiles.firstOrNull { it.name.contains("bienvenida", ignoreCase = true) } ?: FileItem(
                                                name = "bienvenida_ipn.txt",
                                                path = "Documentos/bienvenida_ipn.txt",
                                                isDirectory = false,
                                                sizeBytes = 512L,
                                                formattedSize = "512 B",
                                                lastModifiedEpochMs = System.currentTimeMillis(),
                                                formattedDate = "Hoy",
                                                extension = "txt",
                                                mimeType = "text/plain",
                                                category = FileCategory.TEXT
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuickFileChip(
                                        label = "Archivo 2",
                                        isSelected = activeFileToSend.name.contains("alumnos", ignoreCase = true),
                                        onClick = {
                                            activeFileToSend = availableFiles.firstOrNull { it.name.contains("alumnos", ignoreCase = true) } ?: FileItem(
                                                name = "alumnos_escom.json",
                                                path = "Datos/alumnos_escom.json",
                                                isDirectory = false,
                                                sizeBytes = 1024L,
                                                formattedSize = "1.0 KB",
                                                lastModifiedEpochMs = System.currentTimeMillis(),
                                                formattedDate = "Hoy",
                                                extension = "json",
                                                mimeType = "application/json",
                                                category = FileCategory.JSON
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dispositivos vinculados
                                Text("Dispositivos emparejados detectados:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                val targetDevices = if (devices.isNotEmpty()) devices else listOf(
                                    BluetoothPeerDevice("A4:75:5D:89:C2:11", "Samsung Galaxy A54 (Nodo Par)", isBonded = true),
                                    BluetoothPeerDevice("98:DA:60:01:23:45", "Google Pixel 8 Pro", isBonded = true)
                                )

                                targetDevices.forEach { dev ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onConnectDevice(dev) }
                                            .background(if (connectedPeer?.address == dev.address) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(dev.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(dev.address, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onSendFile(activeFileToSend, dev) },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Enviar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // BOTÓN DE ENVIAR POR BLUETOOTH DEL SISTEMA (A cualquier dispositivo PC o móvil)
                                OutlinedButton(
                                    onClick = { onShareViaSystemBluetooth(activeFileToSend) },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Enviar por Bluetooth del Sistema (A PC o Móvil)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // BOTÓN DE SIMULACIÓN QA (Envío por Bloques en un solo teléfono)
                                Button(
                                    onClick = { onSimulateTransfer(activeFileToSend) },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simular Envío por Bloques (Barra QA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    1 -> {
                        // SECCIÓN RECIBIR ARCHIVO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recepción Segura por RFCOMM:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "El servidor Bluetooth de la app se encuentra escuchando transferencias entrantes con verificación estricta de hash criptográfico SHA-256.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // BOTÓN SIMULACIÓN OFERTA ENTRANTE
                                OutlinedButton(
                                    onClick = onSimulateIncomingOffer,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.CallReceived, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simular Solicitud Entrante con SHA-256 (QA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    2 -> {
                        // SECCIÓN HISTORIAL
                        if (transferHistory.isEmpty()) {
                            Text(
                                text = "No hay transferencias registradas todavía. Pulsa 'Simular Envío' o 'Simular Oferta' para generar registros con hash SHA-256.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            transferHistory.forEach { rec ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (rec.direction == TransferDirection.OUTGOING) Icons.Default.FileUpload else Icons.Default.CallReceived,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (rec.direction == TransferDirection.OUTGOING) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(rec.fileName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "${if (rec.direction == TransferDirection.OUTGOING) "Enviado a" else "Recibido de"} ${rec.peerName} • ${rec.totalBytes} bytes",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "SHA-256: ${rec.sha256Expected.take(20)}...",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 8.sp,
                                                color = Color(0xFF059669)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (rec.status) {
                                                        TransferStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                        TransferStatus.IN_PROGRESS -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                                        else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (rec.status) {
                                                    TransferStatus.COMPLETED -> "VERIFICADO"
                                                    TransferStatus.IN_PROGRESS -> "EN CURSO"
                                                    TransferStatus.CANCELLED -> "CANCELADO"
                                                    TransferStatus.FAILED -> "ERROR"
                                                    else -> "PENDIENTE"
                                                },
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (rec.status) {
                                                    TransferStatus.COMPLETED -> Color(0xFF10B981)
                                                    TransferStatus.IN_PROGRESS -> Color(0xFF0284C7)
                                                    else -> Color(0xFFEF4444)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Cerrar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickFileChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.5.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
