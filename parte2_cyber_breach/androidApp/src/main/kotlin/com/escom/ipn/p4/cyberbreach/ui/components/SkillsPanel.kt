package com.escom.ipn.p4.cyberbreach.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.ipn.p4.cyberbreach.model.SkillType
import com.escom.ipn.p4.cyberbreach.ui.theme.EnergyBarColor

@Composable
fun SkillsPanel(
    currentEnergy: Int,
    activeSkill: SkillType,
    onSkillSelected: (SkillType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Ataque Simple
        SkillButton(
            title = "Ataque",
            costText = "0 EN",
            icon = Icons.Default.CrisisAlert,
            isSelected = activeSkill == SkillType.NORMAL,
            isAvailable = true,
            onClick = { onSkillSelected(SkillType.NORMAL) },
            modifier = Modifier.weight(1f)
        )

        // Ping Sweep (20 EN)
        SkillButton(
            title = "Ping Sweep",
            costText = "20 EN",
            icon = Icons.Default.Radar,
            isSelected = activeSkill == SkillType.PING_SWEEP,
            isAvailable = currentEnergy >= SkillType.PING_SWEEP.energyCost,
            onClick = { onSkillSelected(SkillType.PING_SWEEP) },
            modifier = Modifier.weight(1.1f)
        )

        // DDoS Attack (30 EN)
        SkillButton(
            title = "DDoS 3x1",
            costText = "30 EN",
            icon = Icons.Default.TrackChanges,
            isSelected = activeSkill == SkillType.DDOS,
            isAvailable = currentEnergy >= SkillType.DDOS.energyCost,
            onClick = { onSkillSelected(SkillType.DDOS) },
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
fun SkillButton(
    title: String,
    costText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBorder = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent

    Button(
        onClick = onClick,
        enabled = isAvailable,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        ),
        modifier = modifier.border(if (isSelected) 1.5.dp else 0.dp, activeBorder, RoundedCornerShape(8.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.secondary else if (isAvailable) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = costText,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isAvailable) EnergyBarColor else Color.Gray
            )
        }
    }
}
