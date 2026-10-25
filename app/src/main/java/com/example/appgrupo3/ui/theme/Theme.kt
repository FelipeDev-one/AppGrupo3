package com.example.appgrupo3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulIndustrial,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = AmbarOscuro,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA6),
    onSecondaryContainer = Color(0xFF271900),
    tertiaryContainer = Color(0xFFDCE8F9),
    onTertiaryContainer = Color(0xFF0D2440),
    background = Color(0xFFF8F9FC),
    onBackground = Grafito,
    surface = Color(0xFFF8F9FC),
    onSurface = Grafito,
    surfaceVariant = Acero,
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainer = Color(0xFFEDF0F4),
    surfaceContainerHigh = Color(0xFFE6E9EE),
    error = Color(0xFFBA1A1A)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaro,
    onPrimary = Color(0xFF00315B),
    primaryContainer = Color(0xFF1F4A75),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Ambar,
    onSecondary = Color(0xFF412D00),
    secondaryContainer = Color(0xFF5D4200),
    onSecondaryContainer = Color(0xFFFFDEA6),
    tertiaryContainer = Color(0xFF15304F),
    onTertiaryContainer = Color(0xFFDCE8F9),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF3D4148),
    onSurfaceVariant = Color(0xFFC3C6CF),
    surfaceContainer = Color(0xFF1C2025),
    surfaceContainerHigh = Color(0xFF262A30)
)

@Composable
fun AppGrupo3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
