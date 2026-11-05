package com.escom.ipn.p4.cyberbreach.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.GameMode
import com.escom.ipn.p4.cyberbreach.model.GameState
import com.escom.ipn.p4.cyberbreach.model.NetworkNode
import com.escom.ipn.p4.cyberbreach.model.NodeType
import com.escom.ipn.p4.cyberbreach.ui.theme.CellDestroyedColor
import com.escom.ipn.p4.cyberbreach.ui.theme.EnergyBarColor

@Composable
fun CyberStatusBar(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Fila 1: Puntuación, Modo y Turnos/Tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score
                Column {
                    Text(
                        text = "SCORE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.score}",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Modo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (gameState.gameMode == GameMode.INFILTRATION) "SOLITARIO" else "DUELO VS CPU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Turnos
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TURNOS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${gameState.turns}",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Si es Cyber Duelo vs CPU, mostrar barra de defensa del jugador
            if (gameState.gameMode == GameMode.CPU_DUEL && gameState.playerNodes.isNotEmpty()) {
                val alivePlayerNodes = gameState.totalPlayerNodes - gameState.compromisedPlayerNodes
                val defenseHealthPercent = (alivePlayerNodes.toFloat() / gameState.totalPlayerNodes.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (alivePlayerNodes <= 1) CellDestroyedColor else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEFENSA PROPIA: $alivePlayerNodes/4 NODOS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (alivePlayerNodes <= 1) CellDestroyedColor else MaterialTheme.colorScheme.secondary
                        )
                    }

                    gameState.cpuLastAction?.let { action ->
                        Text(
                            text = action,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFB74D),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Barra de Energía Táctica
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Energía",
                    tint = EnergyBarColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "EN: ${gameState.playerEnergy}/100",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EnergyBarColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { gameState.playerEnergy / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EnergyBarColor,
                    trackColor = Color(0xFF1E2836)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips de estado de los Nodos Enemigos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                gameState.nodes.forEach { node ->
                    NodeStatusBadge(node = node, modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
                }
            }
        }
    }
}

@Composable
fun NodeStatusBadge(node: NetworkNode, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (node.type) {
        NodeType.FIREWALL -> Icons.Default.Security
        NodeType.DATABASE -> Icons.Default.Storage
        NodeType.WEB_SERVER -> Icons.Default.Dns
        NodeType.MAINFRAME -> Icons.Default.Memory
    }

    val (bg, border, tint) = if (node.isCompromised) {
        Triple(CellDestroyedColor.copy(alpha = 0.25f), CellDestroyedColor, CellDestroyedColor)
    } else if (node.hits > 0) {
        Triple(Color(0xFFFF9100).copy(alpha = 0.2f), Color(0xFFFF9100), Color(0xFFFF9100))
    } else {
        Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF94A3B8))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = node.type.displayName, modifier = Modifier.size(16.dp), tint = tint)
            Text(
                text = "${node.hits}/${node.totalSize}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}
