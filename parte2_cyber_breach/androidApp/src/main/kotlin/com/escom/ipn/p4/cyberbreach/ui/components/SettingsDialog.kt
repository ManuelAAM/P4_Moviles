package com.escom.ipn.p4.cyberbreach.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.CyberTheme
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.SaveFormat

@Composable
fun SettingsDialog(
    preferredFormat: SaveFormat,
    currentTheme: CyberTheme,
    isSoundEnabled: Boolean,
    onFormatChange: (SaveFormat) -> Unit,
    onThemeChange: (CyberTheme) -> Unit,
    onToggleSound: () -> Unit,
    onModeChange: (GameMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configuración y Opciones")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sección 1: Formato predeterminado de guardado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.padding(end = 6.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("Formato de guardado predeterminado:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                SaveFormat.values().forEach { fmt ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preferredFormat == fmt,
                            onClick = { onFormatChange(fmt) }
                        )
                        Text("${fmt.name} (${fmt.extension})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sección 2: Tema Visual Institucional
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.padding(end = 6.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("Aspecto Visual Institucional:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = currentTheme == CyberTheme.GUINDA_IPN,
                        onClick = { onThemeChange(CyberTheme.GUINDA_IPN) }
                    )
                    Text("Guinda IPN", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = currentTheme == CyberTheme.AZUL_ESCOM,
                        onClick = { onThemeChange(CyberTheme.AZUL_ESCOM) }
                    )
                    Text("Azul ESCOM", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sección 3: Efectos de Sonido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.padding(end = 6.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("Retroalimentación Sonora:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isSoundEnabled, onCheckedChange = { onToggleSound() })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sección 4: Modo de Juego
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.padding(end = 6.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("Reiniciar partida en modo:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onModeChange(GameMode.INFILTRATION) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Solitario", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onModeChange(GameMode.CPU_DUEL) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Duelo vs CPU", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}
