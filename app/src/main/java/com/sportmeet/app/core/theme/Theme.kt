package com.sportmeet.app.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SportBlue,
    onPrimary = LightSurface,
    primaryContainer = SportBlueContainer,
    onPrimaryContainer = SportBlueDark,
    secondary = SportGreen,
    onSecondary = LightSurface,
    secondaryContainer = SportGreenContainer,
    onSecondaryContainer = SportGreenDark,
    tertiary = SportGold,
    onTertiary = LightSurface,
    tertiaryContainer = SportGoldContainer,
    onTertiaryContainer = SportGoldDark,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = SportBlueLight,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = SportBlueLight,
    secondary = SportGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = SportGreen,
    tertiary = SportGold,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = SportGold,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

@Composable
fun SportMeetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
                }
            } catch (t: Throwable) {
                // Safeguard against WindowInsetsController incompatibilities on custom OEM skins
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
