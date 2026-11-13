package com.escom.ipn.p4.cyberbreach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Warning
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
import com.escom.ipn.p4.cyberbreach.ui.theme.CellDestroyedColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHiddenBorder
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHiddenColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellHitColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellMissColor
import com.escom.ipn.p4.cyberbreach.ui.theme.CellMissDot

@Composable
fun CyberGrid(
    grid: List<Int>,
    gridSize: Int = 8,
    targetedCell: Int? = null,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnLabels = listOf("A", "B", "C", "D", "E", "F", "G", "H")

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
            // Espacio vacío para la esquina
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
                // Etiqueta de número de fila
                Text(
                    text = "${r + 1}",
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Celdas de la fila
                for (c in 0 until gridSize) {
                    val index = r * gridSize + c
                    val cellState = grid.getOrElse(index) { CELL_HIDDEN }
                    val isTargeted = (targetedCell == index)

                    CyberCell(
                        cellState = cellState,
                        isSensorTargeted = isTargeted,
                        onClick = { onCellClick(index) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CyberCell(
    cellState: Int,
    isSensorTargeted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor) = when {
        isSensorTargeted -> Pair(Color(0x3300E5FF), Color(0xFF00E5FF))
        cellState == CELL_HIDDEN -> Pair(CellHiddenColor, CellHiddenBorder)
        cellState == CELL_MISS -> Pair(CellMissColor, CellMissDot.copy(alpha = 0.5f))
        cellState == CELL_HIT -> Pair(CellHitColor.copy(alpha = 0.25f), CellHitColor)
        cellState == CELL_DESTROYED -> Pair(CellDestroyedColor.copy(alpha = 0.35f), CellDestroyedColor)
        else -> Pair(CellHiddenColor, CellHiddenBorder)
    }

    val borderWidth = if (isSensorTargeted) 2.dp else 1.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSensorTargeted) {
            Icon(
                imageVector = Icons.Default.Adjust,
                contentDescription = "Mira Giroscopio",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF00E5FF)
            )
        } else {
            when (cellState) {
                CELL_HIDDEN -> {
                    // Celda oculta enigmática
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(CellHiddenBorder.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    )
                }
                CELL_MISS -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fallo",
                        modifier = Modifier.size(14.dp),
                        tint = CellMissDot
                    )
                }
                CELL_HIT -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Impacto",
                        modifier = Modifier.size(16.dp),
                        tint = CellHitColor
                    )
                }
                CELL_DESTROYED -> {
                    Icon(
                        imageVector = Icons.Default.Dangerous,
                        contentDescription = "Compromiso",
                        modifier = Modifier.size(18.dp),
                        tint = CellDestroyedColor
                    )
                }
            }
        }
    }
}
