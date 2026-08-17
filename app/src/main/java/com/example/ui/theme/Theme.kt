package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Minimalist Shadcn Shapes (8-12px, clean and subtle)
val ShadcnShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

val SophisticatedShapes = ShadcnShapes

private val ShadcnColorScheme = lightColorScheme(
    primary = ShadcnPrimary,
    onPrimary = ShadcnPrimaryForeground,
    primaryContainer = ShadcnMutedBackground,
    onPrimaryContainer = ShadcnForeground,
    secondary = ShadcnSecondary,
    onSecondary = ShadcnSecondaryForeground,
    secondaryContainer = ShadcnMutedBackground,
    onSecondaryContainer = ShadcnForeground,
    tertiary = ShadcnWarning,
    onTertiary = Color.White,
    tertiaryContainer = ShadcnWarningBg,
    onTertiaryContainer = ShadcnWarning,
    background = ShadcnBackground,
    onBackground = ShadcnForeground,
    surface = ShadcnCard,
    onSurface = ShadcnForeground,
    surfaceVariant = ShadcnMutedBackground,
    onSurfaceVariant = ShadcnMutedText,
    outline = ShadcnBorder,
    outlineVariant = ShadcnBorderSubtle,
    error = ShadcnDestructive,
    onError = Color.White
)

@Composable
fun ZyneOffersTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShadcnColorScheme,
        typography = Typography,
        shapes = ShadcnShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZyneOffersTheme(darkTheme = false, dynamicColor = false, content = content)
}

