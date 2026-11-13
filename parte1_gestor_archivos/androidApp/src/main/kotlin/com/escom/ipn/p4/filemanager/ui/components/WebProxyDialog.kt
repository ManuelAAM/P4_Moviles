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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
                Column {
                    Text("Consulta Web Vía Bluetooth", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Ejercicio 3 • Proxy Relay / Mitigación SSRF", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner explicativo pedagógico de Ejercicio 3
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Arquitectura Gateway Intermediario", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Dispositivo B (Offline) consulta APIs web a través de Dispositivo A (Gateway Online) por Bluetooth RFCOMM. Dispositivo A bloquea destinos privados (SSRF) y valida la whitelist.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de entrada de URL
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("URL HTTPS de Recurso Web", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Chips de prueba rápida para captura de pantalla
                Text("Recursos HTTPS de prueba autorizados:", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PresetChip(
                        label = "Posts",
                        url = "https://jsonplaceholder.typicode.com/posts/1",
                        onClick = { urlText = it; onExecuteRequest(it) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        label = "Hora CDMX",
                        url = "https://worldtimeapi.org/api/timezone/America/Mexico_City",
                        onClick = { urlText = it; onExecuteRequest(it) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        label = "PokeAPI",
                        url = "https://pokeapi.co/api/v2/pokemon/ditto",
                        onClick = { urlText = it; onExecuteRequest(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chip de prueba para Demostrar Bloqueo de Ataques SSRF
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .clickable {
                            val ssrfUrl = "https://192.168.1.1/admin_router"
                            urlText = ssrfUrl
                            onExecuteRequest(ssrfUrl)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("🛡️ Demostración de Mitigación SSRF (Click para probar)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text("Envía https://192.168.1.1/admin para verificar el bloqueo de IPs privadas", fontSize = 9.sp, color = Color(0xFFB91C1C))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón de ejecución
                Button(
                    onClick = { onExecuteRequest(urlText) },
                    enabled = !isLoading && urlText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transfiriendo por Bluetooth RFCOMM...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Solicitar Recurso Vía Bluetooth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta de Resultado Recibido
                if (lastResponse != null) {
                    val isSuccess = lastResponse.statusCode in 200..299
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(8.dp)),
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
                                        .background(
                                            when {
                                                lastResponse.fromCache -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                isSuccess -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            lastResponse.fromCache -> "🟡 COPIA EN CACHÉ LOCAL"
                                            isSuccess -> "🟢 GATEWAY BLUETOOTH OK"
                                            else -> "🔴 BLOQUEADO / ERROR"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            lastResponse.fromCache -> Color(0xFFD97706)
                                            isSuccess -> Color(0xFF047857)
                                            else -> Color(0xFFDC2626)
                                        }
                                    )
                                }

                                Text(
                                    text = "HTTP ${lastResponse.statusCode} • ${lastResponse.durationMs} ms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isSuccess) "Cuerpo de respuesta recibido:" else "Detalle de seguridad / rechazo:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = lastResponse.body.take(500) + if (lastResponse.body.length > 500) "\n... [truncado]" else "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = if (isSuccess) Color(0xFF38BDF8) else Color(0xFFF87171)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Sección Caché Local Persistente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Caché Local Persistente:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    if (cacheRecords.isNotEmpty()) {
                        TextButton(onClick = onClearCache, modifier = Modifier.height(26.dp)) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Limpiar", fontSize = 10.sp)
                        }
                    }
                }

                if (cacheRecords.isEmpty()) {
                    Text("No hay respuestas guardadas en caché local.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.url, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                                Text("Guardado: ${rec.humanReadableDate}", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
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

@Composable
private fun PresetChip(
    label: String,
    url: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick(url) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
