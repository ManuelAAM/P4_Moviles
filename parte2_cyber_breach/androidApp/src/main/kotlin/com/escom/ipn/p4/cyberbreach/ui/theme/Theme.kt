package com.escom.ipn.p4.cyberbreach.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.escom.ipn.p4.cyberbreach.model.CyberTheme

private val GuindaColorScheme = darkColorScheme(
    primary = CyberGuindaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF530030),
    onPrimaryContainer = Color(0xFFFFD8E6),
    secondary = CyberGuindaAccent,
    background = CyberGuindaBackground,
    surface = CyberGuindaSurface,
    onBackground = Color(0xFFF1DFE6),
    onSurface = Color(0xFFF1DFE6)
)

private val AzulColorScheme = darkColorScheme(
    primary = CyberAzulPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003258),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = CyberAzulAccent,
    background = CyberAzulBackground,
    surface = CyberAzulSurface,
    onBackground = Color(0xFFDFE8F1),
    onSurface = Color(0xFFDFE8F1)
)

@Composable
fun CyberBreachTheme(
    cyberTheme: CyberTheme = CyberTheme.GUINDA_IPN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (cyberTheme) {
        CyberTheme.GUINDA_IPN -> GuindaColorScheme
        CyberTheme.AZUL_ESCOM -> AzulColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
