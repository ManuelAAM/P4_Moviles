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
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Telemetría de Sensores Físicos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Selector de modo simulación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Modo Simulación / Mock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Para emuladores y pruebas automatizadas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isSimulationMode, onCheckedChange = { onToggleSimulation() })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sensor 1: Acelerómetro
                SensorCard(
                    title = "Acelerómetro Lineal",
                    sensorData = accelData,
                    unit = "m/s²",
                    extraInfo = "Magnitud: ${"%.2f".format(accelData.magnitude)} m/s²"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sensor 2: Giroscopio
                SensorCard(
                    title = "Giroscopio Angular",
                    sensorData = gyroData,
                    unit = "rad/s",
                    extraInfo = "Velocidad rotacional en 3 ejes"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Inclinación estimada (Tilt)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Orientación Dinámica (Tilt Angles)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pitch (Inclinación frontal): ${"%.1f".format(tiltState.pitch)}°",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Roll (Inclinación lateral): ${"%.1f".format(tiltState.roll)}°",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Gesto activo: Agitar el dispositivo (Shake) actualiza el directorio.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (isSimulationMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Inyección de valores para pruebas QA:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 0f, 9.8f, 0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Normal (1G)", fontSize = 9.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 15f, 15f, 2f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Shake (>13)", fontSize = 9.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { onInjectSimulated(SensorType.ACCELEROMETER, 0f, 0f, 0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cero (0)", fontSize = 9.sp)
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
private fun SensorCard(
    title: String,
    sensorData: SensorData,
    unit: String,
    extraInfo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            SensorStatus.ACTIVE -> if (sensorData.isSimulated) "SIMULADO" else "ACTIVO"
                            SensorStatus.PAUSED -> "PAUSADO"
                            SensorStatus.UNAVAILABLE -> "NO DISPONIBLE"
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AxisItem("Eje X", sensorData.x, unit)
                AxisItem("Eje Y", sensorData.y, unit)
                AxisItem("Eje Z", sensorData.z, unit)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = extraInfo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AxisItem(label: String, value: Float, unit: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "${"%.2f".format(value)} $unit",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
