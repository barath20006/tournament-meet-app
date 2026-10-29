package com.sportmeet.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Athletic & High-Energy Sports Primary Theme Colors
val SportPrimary = Color(0xFF1E50FF)       // Electric Stadium Blue
val SportPrimaryDark = Color(0xFF1036BF)   // Deep Championship Navy
val SportPrimaryLight = Color(0xFF4C75FF)  // Vibrant Stadium Light
val SportPrimaryContainer = Color(0xFFEEF2FF)

// Legacy alias for compatibility across files
val SportBlue = SportPrimary
val SportBlueDark = SportPrimaryDark
val SportBlueLight = SportPrimaryLight
val SportBlueContainer = SportPrimaryContainer

// Secondary - Athletic Emerald & Velocity Mint
val SportGreen = Color(0xFF059669)         // Crisp Emerald
val SportGreenDark = Color(0xFF047857)
val SportGreenLight = Color(0xFF34D399)
val SportGreenContainer = Color(0xFFECFDF5)
val SportNeonGreen = Color(0xFF00E676)

// Tertiary - Championship Trophy Gold & Amber
val SportGold = Color(0xFFFFB800)          // Radiant Trophy Gold
val SportGoldDark = Color(0xFFD97706)
val SportGoldLight = Color(0xFFFFD566)
val SportGoldContainer = Color(0xFFFFFBEB)
val SportGoldBright = Color(0xFFFFB800)

// Accent - Match Flame Red / Coral
val SportRed = Color(0xFFFF334B)           // Match Fire Red
val SportRedDark = Color(0xFFDC2626)
val SportRedContainer = Color(0xFFFEF2F2)

val SportPurple = Color(0xFF8B5CF6)
val SportCyan = Color(0xFF06B6D4)

// Neutral surfaces - Light (Crisp, High-Contrast Modern Athletic Canvas)
val LightBackground = Color(0xFFF5F7FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBF0F8)
val LightBorder = Color(0xFFDFE6F1)
val LightTextPrimary = Color(0xFF090E1A)
val LightTextSecondary = Color(0xFF5B6983)

// Neutral surfaces - Dark (Deep Space Stadium Obsidian)
val DarkBackground = Color(0xFF080D1A)
val DarkSurface = Color(0xFF10182C)
val DarkSurfaceVariant = Color(0xFF18233C)
val DarkBorder = Color(0xFF223050)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)

// Athletic Gradients
val AthleticHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E50FF),
        Color(0xFF4F46E5),
        Color(0xFF7C3AED)
    )
)

val TrophyGoldBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFB800),
        Color(0xFFFF8A00)
    )
)

val EmeraldLiveBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00E676),
        Color(0xFF059669)
    )
)

val DarkGlassBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1E293B).copy(alpha = 0.9f),
        Color(0xFF0F172A).copy(alpha = 0.98f)
    )
)
