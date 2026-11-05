package com.escom.ipn.p4.cyberbreach.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.ui.theme.CellDestroyedColor

@Composable
fun GameOverDialog(
    gameState: GameState,
    onRestart: () -> Unit,
    onSaveGame: () -> Unit,
    onMainMenu: () -> Unit
) {
    val isVictory = gameState.isVictory

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Dangerous,
                    contentDescription = null,
                    tint = if (isVictory) Color(0xFFFFD700) else CellDestroyedColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isVictory) {
                        "¡INFILTRACIÓN EXITOSA!"
                    } else if (gameState.gameMode == GameMode.CPU_DUEL) {
                        "¡DEFENSA COMPROMETIDA!"
                    } else {
                        "¡SISTEMA BLOQUEADO!"
                    },
                    color = if (isVictory) Color(0xFFFFD700) else CellDestroyedColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isVictory) {
                        "Has neutralizado el 100% de la infraestructura de red enemiga (Firewall, Base de Datos, Servidor Web y Mainframe)."
                    } else if (gameState.gameMode == GameMode.CPU_DUEL) {
                        "La CPU enemiga atacó tu red y destruyó todos tus nodos de infraestructura. Has sido derrotado en este ciberduelo."
                    } else {
                        "La incursión ha sido interrumpida antes de comprometer todos los nodos enemigos."
                    },
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Estadísticas Finales:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Modo: ${gameState.gameMode.displayName}", fontSize = 12.sp)
                Text("• Puntuación total: ${gameState.score} pts", fontSize = 12.sp)
                Text("• Turnos ejecutados: ${gameState.turns}", fontSize = 12.sp)
                Text("• Tiempo de sesión: ${gameState.elapsedSeconds} segundos", fontSize = 12.sp)
                Text("• Nodos enemigos comprometidos: ${gameState.compromisedNodes}/${gameState.totalNodes}", fontSize = 12.sp)
                if (gameState.gameMode == GameMode.CPU_DUEL) {
                    Text("• Nodos propios supervivientes: ${gameState.totalPlayerNodes - gameState.compromisedPlayerNodes}/${gameState.totalPlayerNodes}", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Row {
                OutlinedButton(onClick = onMainMenu) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Menú Principal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSaveGame) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onRestart) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Jugar de nuevo")
            }
        }
    )
}
