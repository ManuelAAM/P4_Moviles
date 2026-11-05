package com.escom.ipn.p4.cyberbreach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.CELL_DESTROYED
import com.escom.ipn.p4.cyberbreach.model.CELL_HIDDEN
import com.escom.ipn.p4.cyberbreach.model.CELL_HIT
import com.escom.ipn.p4.cyberbreach.model.CELL_MISS
import com.escom.ipn.p4.cyberbreach.model.NetworkNode
import com.escom.ipn.p4.cyberbreach.model.NodeType
import com.escom.ipn.p4.cyberbreach.ui.theme.CellDestroyedColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHiddenBorder
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHiddenColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHitColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellMissColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellMissDot

@Composable
fun PlayerDefenseGrid(
    playerGrid: List<Int>,
    playerNodes: List<NetworkNode>,
    gridSize: Int = 8,
    modifier: Modifier = Modifier
) {
    val columnLabels = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    val cellToNodeMap = mutableMapOf<Int, NetworkNode>()
    playerNodes.forEach { node ->
        node.cells.forEach { cell ->
            cellToNodeMap[cell] = node
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera de columnas A-H
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(0.7f))

            for (c in 0 until gridSize) {
                Text(
                    text = columnLabels.getOrElse(c) { "$c" },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Filas 1-8 con sus celdas
        for (r in 0 until gridSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${r + 1}",
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                for (c in 0 until gridSize) {
                    val index = r * gridSize + c
                    val cellState = playerGrid.getOrElse(index) { CELL_HIDDEN }
                    val assignedNode = cellToNodeMap[index]

                    PlayerDefenseCell(
                        cellState = cellState,
                        node = assignedNode,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Leyenda descriptiva para la defensa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF1E3A5F), label = "Nodo Activo")
            LegendItem(color = CellMissDot, label = "CPU Falló")
            LegendItem(color = CellHitColor, label = "CPU Impactó")
            LegendItem(color = CellDestroyedColor, label = "Destruido")
        }
    }
}

@Composable
fun PlayerDefenseCell(
    cellState: Int,
    node: NetworkNode?,
    modifier: Modifier = Modifier
) {
    val isOccupied = node != null

    val (bgColor, borderColor) = when (cellState) {
        CELL_DESTROYED -> Pair(CellDestroyedColor.copy(alpha = 0.4f), CellDestroyedColor)
        CELL_HIT -> Pair(CellHitColor.copy(alpha = 0.35f), CellHitColor)
        CELL_MISS -> Pair(CellMissColor, CellMissDot.copy(alpha = 0.5f))
        else -> {
            if (isOccupied) {
                Pair(Color(0xFF0F2E4C), Color(0xFF38BDF8))
            } else {
                Pair(CellHiddenColor, CellHiddenBorder)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (cellState) {
            CELL_DESTROYED -> {
                Icon(
                    Icons.Default.Dangerous,
                    contentDescription = "Destruido",
                    tint = CellDestroyedColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            CELL_HIT -> {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Impactado",
                    tint = CellHitColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            CELL_MISS -> {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = "Fallo CPU",
                    tint = CellMissDot,
                    modifier = Modifier.size(8.dp)
                )
            }
            else -> {
                if (isOccupied && node != null) {
                    val initial = when (node.type) {
                        NodeType.FIREWALL -> "F"
                        NodeType.DATABASE -> "D"
                        NodeType.WEB_SERVER -> "W"
                        NodeType.MAINFRAME -> "M"
                    }
                    Text(
                        text = initial,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = " $label",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
