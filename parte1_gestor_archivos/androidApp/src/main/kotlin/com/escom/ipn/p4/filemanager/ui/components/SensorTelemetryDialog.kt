package com.escom.ipn.p4.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.escom.ipn.p4.filemanager.model.MotionTiltState
import com.escom.ipn.p4.filemanager.model.SensorData
import com.escom.ipn.p4.filemanager.model.SensorStatus
import com.escom.ipn.p4.filemanager.model.SensorType
import java.util.Locale

@Composable
fun SensorTelemetryDialog(
    accelData: SensorData,
    gyroData: SensorData,
    tiltState: MotionTiltState,
    isSimulationMode: Boolean,
    onToggleSimulation: () -> Unit,
    onInjectSimulated: (SensorType, Float, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    // Control de congelamiento/pausa para captura estática limpia en pruebas QA
    var isFrozen by remember { mutableStateOf(false) }
    var frozenAccel by remember { mutableStateOf(accelData) }
    var frozenGyro by remember { mutableStateOf(gyroData) }
    var frozenTilt by remember { mutableStateOf(tiltState) }

    if (!isFrozen) {
        frozenAccel = accelData
        frozenGyro = gyroData
        frozenTilt = tiltState
    }

    val displayAccel = if (isFrozen) frozenAccel else accelData
    val displayGyro = if (isFrozen) frozenGyro else gyroData
    val displayTilt = if (isFrozen) frozenTilt else tiltState

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telemetría de Sensores",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                }

                // Botón Congelar / Reanudar Lecturas
                FilledTonalButton(
                    onClick = {
                        if (!isFrozen) {
                            frozenAccel = accelData
                            frozenGyro = gyroData
                            frozenTilt = tiltState
                        }
                        isFrozen = !isFrozen
                    },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFrozen) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFrozen) "Reanudar" else "Congelar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Notificación de estado pausado/congelado para la captura
                if (isFrozen) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                    ) {
                        Text(
                            text = "⏸️ Lecturas congeladas para captura de pantalla fija. Pulsa 'Reanudar' para continuar en tiempo real.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Selector de modo simulación / QA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Modo Mock / QA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Inyección simulada de eventos", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isSimulationMode, onCheckedChange = { onToggleSimulation() })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sensor 1: Acelerómetro Lineal
                SensorCardModern(
                    title = "Acelerómetro Lineal",
                    sensorData = displayAccel,
                    unit = "m/s²",
                    extraInfo = "Magnitud total: ${String.format(Locale.US, "%.2f", displayAccel.magnitude)} m/s²",
                    maxExpected = 15f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sensor 2: Giroscopio Angular
                SensorCardModern(
                    title = "Giroscopio Angular",
                    sensorData = displayGyro,
                    unit = "rad/s",
                    extraInfo = "Velocidad angular (Rotación 3D)",
                    maxExpected = 8f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Orientación Dinámica (Tilt & Shake)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Orientación y Gestos Físicos", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Pitch Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("Pitch (Frontal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${String.format(Locale.US, "%+5.1f", displayTilt.pitch)}°",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (displayTilt.pitch > 10f) "Inclinado frontal" else if (displayTilt.pitch < -10f) "Inclinado dorsal" else "Nivel plano",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // Roll Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("Roll (Lateral)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${String.format(Locale.US, "%+5.1f", displayTilt.roll)}°",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (displayTilt.roll > 10f) "Inclinado izq." else if (displayTilt.roll < -10f) "Inclinado der." else "Nivel centrado",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Shake Status Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gesto Shake activo: Agita el móvil para refrescar la lista de archivos.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isSimulationMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Inyección manual de pruebas QA:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 0f, 9.8f, 0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("1G (Tierra)", fontSize = 9.sp)
                        }
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 16f, 16f, 2f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Shake (>13)", fontSize = 9.sp)
                        }
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 0f, 0f, 0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Gravedad 0", fontSize = 9.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}

@Composable
private fun SensorCardModern(
    title: String,
    sensorData: SensorData,
    unit: String,
    extraInfo: String,
    maxExpected: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (sensorData.status) {
                                SensorStatus.ACTIVE -> Color(0xFF10B981).copy(alpha = 0.2f)
                                SensorStatus.PAUSED -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                SensorStatus.UNAVAILABLE -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (sensorData.status) {
                            SensorStatus.ACTIVE -> if (sensorData.isSimulated) "MOCK" else "EN VIVO"
                            SensorStatus.PAUSED -> "PAUSA"
                            SensorStatus.UNAVAILABLE -> "N/D"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (sensorData.status) {
                            SensorStatus.ACTIVE -> Color(0xFF10B981)
                            SensorStatus.PAUSED -> Color(0xFFF59E0B)
                            SensorStatus.UNAVAILABLE -> Color(0xFFEF4444)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Distribución fija y equilibrada de ejes X, Y, Z
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AxisItemPill("Eje X", sensorData.x, unit, Modifier.weight(1f), maxExpected)
                AxisItemPill("Eje Y", sensorData.y, unit, Modifier.weight(1f), maxExpected)
                AxisItemPill("Eje Z", sensorData.z, unit, Modifier.weight(1f), maxExpected)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = extraInfo, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AxisItemPill(
    label: String,
    value: Float,
    unit: String,
    modifier: Modifier = Modifier,
    maxExpected: Float
) {
    val progressFraction = (kotlin.math.abs(value) / maxExpected).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = String.format(Locale.US, "%+5.2f", value),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(text = unit, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}
