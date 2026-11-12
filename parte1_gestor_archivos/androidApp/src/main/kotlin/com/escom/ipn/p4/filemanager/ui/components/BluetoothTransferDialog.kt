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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.filemanager.model.BluetoothPeerDevice
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.FileTransferRecord
import com.escom.ipn.p4.filemanager.model.TransferDirection
import com.escom.ipn.p4.filemanager.model.TransferStatus

@Composable
fun BluetoothTransferDialog(
    selectedFile: FileItem?,
    devices: List<BluetoothPeerDevice>,
    connectedPeer: BluetoothPeerDevice?,
    activeTransfer: FileTransferRecord?,
    transferHistory: List<FileTransferRecord>,
    pendingOffer: FileTransferMetadataPacket?,
    onConnectDevice: (BluetoothPeerDevice) -> Unit,
    onSendFile: (FileItem, BluetoothPeerDevice) -> Unit,
    onSimulateTransfer: (FileItem) -> Unit,
    onAcceptIncoming: () -> Unit,
    onRejectIncoming: () -> Unit,
    onCancelTransfer: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Text("Transferencia de Archivos por Bluetooth", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (connectedPeer != null) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (connectedPeer != null) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (connectedPeer != null) "Conectado con: ${connectedPeer.name}" else "Sin conexión activa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Límite didáctico: 5 MB por archivo. Formatos: TXT, JSON, XML, JPG",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Oferta entrante pendiente de confirmación (Ejercicio 2 Requisito de aceptación explícita)
                if (pendingOffer != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Solicitud de Transferencia Entrante",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Archivo: ${pendingOffer.fileName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Tamaño: ${pendingOffer.totalBytes} bytes", fontSize = 10.sp)
                            Text("Emisor: ${pendingOffer.senderName}", fontSize = 10.sp)
                            Text("SHA-256: ${pendingOffer.sha256.take(16)}...", fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = onRejectIncoming, modifier = Modifier.height(34.dp)) {
                                    Text("Rechazar", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = onAcceptIncoming, modifier = Modifier.height(34.dp)) {
                                    Text("Aceptar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Archivo seleccionado para enviar
                if (selectedFile != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Archivo a enviar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(selectedFile.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Tamaño: ${selectedFile.formattedSize} (${selectedFile.sizeBytes} bytes)", fontSize = 10.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Dispositivos emparejados disponibles:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))

                            devices.forEach { dev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onConnectDevice(dev) }
                                        .background(if (connectedPeer?.address == dev.address) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(dev.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(dev.address, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { onSendFile(selectedFile, dev) },
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Enviar", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { onSimulateTransfer(selectedFile) },
                                modifier = Modifier.fillMaxWidth().height(34.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Probar Envío por Bloques (Simulado)", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Transferencia activa en progreso
                if (activeTransfer != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeTransfer.direction == TransferDirection.OUTGOING) "Enviando: ${activeTransfer.fileName}" else "Recibiendo: ${activeTransfer.fileName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text("${activeTransfer.progressPercentage}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { activeTransfer.progressFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${activeTransfer.transferredBytes} / ${activeTransfer.totalBytes} bytes", fontSize = 10.sp)
                                TextButton(onClick = onCancelTransfer) {
                                    Text("Cancelar", color = Color(0xFFEF4444), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Historial de transferencias
                Text("Historial de Transferencias:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                if (transferHistory.isEmpty()) {
                    Text("No hay transferencias registradas aún.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    transferHistory.take(5).forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.fileName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${if (rec.direction == TransferDirection.OUTGOING) "Enviado a" else "Recibido de"} ${rec.peerName} • SHA-256 Verificado",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (rec.status) {
                                            TransferStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                            TransferStatus.IN_PROGRESS -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                            TransferStatus.CANCELLED -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            TransferStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            TransferStatus.PENDING -> Color.Gray.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rec.status.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (rec.status) {
                                        TransferStatus.COMPLETED -> Color(0xFF10B981)
                                        TransferStatus.IN_PROGRESS -> Color(0xFF38BDF8)
                                        TransferStatus.CANCELLED -> Color(0xFFF59E0B)
                                        TransferStatus.FAILED -> Color(0xFFEF4444)
                                        TransferStatus.PENDING -> Color.Gray
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
