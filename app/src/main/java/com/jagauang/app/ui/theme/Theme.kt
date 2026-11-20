package com.jagauang.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = IndigoTint,
    onPrimaryContainer = IndigoDeep,
    secondary = Indigo,
    onSecondary = Color.White,
    secondaryContainer = BlueTint,
    onSecondaryContainer = IndigoDeep,
    tertiary = Violet,
    onTertiary = Color.White,
    tertiaryContainer = VioletTint,
    onTertiaryContainer = Color(0xFF4C1D95),
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceTint = Surface,
    surfaceBright = Surface,
    surfaceDim = SurfaceMuted,
    surfaceContainerLowest = Surface,
    surfaceContainerLow = Color(0xFFF9FAFC),
    surfaceContainer = SurfaceMuted,
    surfaceContainerHigh = Color(0xFFEFF2F7),
    surfaceContainerHighest = Color(0xFFE7ECF3),
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = Muted,
    error = Expense,
    onError = Color.White,
    errorContainer = ExpenseTint,
    onErrorContainer = Expense,
    outline = Control,
    outlineVariant = Divider,
)

@Composable
fun JagaUangTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = JagaUangTypography,
        content = content,
    )
}
