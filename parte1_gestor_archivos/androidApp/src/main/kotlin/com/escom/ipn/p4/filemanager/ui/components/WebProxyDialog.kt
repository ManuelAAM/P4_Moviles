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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.escom.ipn.p4.filemanager.model.HttpProxyResponse
import com.escom.ipn.p4.filemanager.model.WebCacheRecord
import com.escom.ipn.p4.filemanager.service.BluetoothProtocolService

@Composable
fun WebProxyDialog(
    initialUrl: String,
    lastResponse: HttpProxyResponse?,
    cacheRecords: List<WebCacheRecord>,
    isLoading: Boolean,
    onExecuteRequest: (String) -> Unit,
    onClearCache: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consulta Web Vía Bluetooth (Proxy Relay)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner explicativo de arquitectura Dispositivo A vs B
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Arquitectura de Consulta Controlada", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            text = "Dispositivo B (Offline) solicita el recurso a Dispositivo A (Gateway con Internet) a través de Bluetooth. Whitelist activa contra ataques SSRF.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campo de entrada de URL
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("URL HTTPS Autorizada", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Chips de accesos rápidos autorizados
                Text("Recursos HTTPS de prueba autorizados:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                BluetoothProtocolService.DEMO_PROXY_ENDPOINTS.forEach { endpoint ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                urlText = endpoint
                                onExecuteRequest(endpoint)
                            }
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Http, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = endpoint,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón de solicitar recurso
                Button(
                    onClick = { onExecuteRequest(urlText) },
                    enabled = !isLoading && urlText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transfiriendo por Bluetooth...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solicitar Recurso Vía Bluetooth", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resultado recibido
                if (lastResponse != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (lastResponse.statusCode in 200..299) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (lastResponse.fromCache) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (lastResponse.fromCache) "🟡 COPIA EN CACHÉ LOCAL" else "🔴 RED VÍA GATEWAY BLUETOOTH",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lastResponse.fromCache) Color(0xFFD97706) else Color(0xFF0284C7)
                                    )
                                }

                                Text(
                                    text = "Status: ${lastResponse.statusCode} • ${lastResponse.durationMs}ms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Contenido recibido:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = lastResponse.body.take(600) + if (lastResponse.body.length > 600) "\n... [truncado para visualización]" else "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Historial de Caché Local
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Caché Local Persistente:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (cacheRecords.isNotEmpty()) {
                        TextButton(onClick = onClearCache) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar", fontSize = 10.sp)
                        }
                    }
                }

                if (cacheRecords.isEmpty()) {
                    Text("No hay respuestas guardadas en caché.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    cacheRecords.forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    urlText = rec.url
                                    onExecuteRequest(rec.url)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.url, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                                Text("Guardado: ${rec.humanReadableDate}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
