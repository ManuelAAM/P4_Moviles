package com.escom.ipn.p4.filemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.escom.ipn.p4.filemanager.model.AppColorTheme

private val GuindaLightColorScheme = lightColorScheme(
    primary = IPNGuindaPrimary,
    onPrimary = IPNGuindaOnPrimary,
    primaryContainer = IPNGuindaPrimaryContainer,
    onPrimaryContainer = IPNGuindaOnPrimaryContainer,
    secondary = IPNGuindaSecondary,
    background = IPNGuindaBackgroundLight,
    surface = IPNGuindaSurfaceLight,
    onBackground = Color(0xFF22191D),
    onSurface = Color(0xFF22191D)
)

private val GuindaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB0D0),
    onPrimary = Color(0xFF530030),
    primaryContainer = Color(0xFF741544),
    onPrimaryContainer = Color(0xFFFFD8E6),
    secondary = Color(0xFFFFB0D0),
    background = IPNGuindaBackgroundDark,
    surface = IPNGuindaSurfaceDark,
    onBackground = Color(0xFFEFE0E4),
    onSurface = Color(0xFFEFE0E4)
)

private val AzulLightColorScheme = lightColorScheme(
    primary = ESCOMAzulPrimary,
    onPrimary = ESCOMAzulOnPrimary,
    primaryContainer = ESCOMAzulPrimaryContainer,
    onPrimaryContainer = ESCOMAzulOnPrimaryContainer,
    secondary = ESCOMAzulSecondary,
    background = ESCOMAzulBackgroundLight,
    surface = ESCOMAzulSurfaceLight,
    onBackground = Color(0xFF191C20),
    onSurface = Color(0xFF191C20)
)

private val AzulDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFF8BCEFF),
    background = ESCOMAzulBackgroundDark,
    surface = ESCOMAzulSurfaceDark,
    onBackground = Color(0xFFE1E2E8),
    onSurface = Color(0xFFE1E2E8)
)

@Composable
fun FileManagerTheme(
    colorTheme: AppColorTheme = AppColorTheme.GUINDA_IPN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (colorTheme) {
        AppColorTheme.GUINDA_IPN -> if (darkTheme) GuindaDarkColorScheme else GuindaLightColorScheme
        AppColorTheme.AZUL_ESCOM -> if (darkTheme) AzulDarkColorScheme else AzulLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
