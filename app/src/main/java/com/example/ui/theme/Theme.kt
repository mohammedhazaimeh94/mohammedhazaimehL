package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val MadarDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = CrispWhite,
    secondary = SignalAmber,
    onSecondary = ObsidianDeep,
    secondaryContainer = SignalAmberContainer,
    onSecondaryContainer = CrispWhite,
    tertiary = EmeraldHealth,
    onTertiary = ObsidianDeep,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = CrispWhite,
    background = ObsidianDeep,
    onBackground = CrispWhite,
    surface = ObsidianSurface,
    onSurface = CrispWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = SoftSlate,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianCardElevated,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder.copy(alpha = 0.55f),
    error = AlertCrimson,
    onError = CrispWhite,
    errorContainer = AlertCrimsonContainer,
    onErrorContainer = CrispWhite
)

private val MadarShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MadarLauncherTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = MadarDarkColorScheme,
            typography = MadarTypography,
            shapes = MadarShapes,
            content = content
        )
    }
}
