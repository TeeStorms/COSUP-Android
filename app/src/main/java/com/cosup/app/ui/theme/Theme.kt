package com.cosup.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colours copied from the website's site.css (:root and [data-theme="dark"]).
val CosupGreen = Color(0xFF3BA53E)
val CosupGreenDark = Color(0xFF2D8A30)
val CosupGreenPale = Color(0xFFE8F5EE)
val CosupInk = Color(0xFF0D1F15)
val CosupRed = Color(0xFFE05A3A)
val CosupAccent = Color(0xFFF0C93A)

private val LightColors = lightColorScheme(
    primary = CosupGreen,
    onPrimary = Color.White,
    primaryContainer = CosupGreenPale,
    onPrimaryContainer = CosupInk,
    secondary = CosupGreenDark,
    onSecondary = Color.White,
    tertiary = CosupAccent,
    background = Color(0xFFF7FAF8),
    onBackground = CosupInk,
    surface = Color.White,
    onSurface = CosupInk,
    surfaceVariant = Color(0xFFEEF6F1),
    onSurfaceVariant = Color(0xFF6B7C72),
    outline = Color(0xFFD0E8D8),
    error = CosupRed
)

private val DarkColors = darkColorScheme(
    primary = CosupGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF12261A),
    onPrimaryContainer = Color(0xFFEAF3ED),
    secondary = Color(0xFF6FCB72),
    onSecondary = CosupInk,
    tertiary = CosupAccent,
    background = Color(0xFF080F0B),
    onBackground = Color(0xFFEAF3ED),
    surface = Color(0xFF101C15),
    onSurface = Color(0xFFEAF3ED),
    surfaceVariant = Color(0xFF0B1610),
    onSurfaceVariant = Color(0xFF93A79B),
    outline = Color(0xFF1F3427),
    error = Color(0xFFFF8A6B)
)

private val base = Typography()
private val CosupTypography = base.copy(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 23.sp)
)

private val CosupShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun CosupTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CosupTypography,
        shapes = CosupShapes,
        content = content
    )
}
