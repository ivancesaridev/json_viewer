package org.ivancesari.jsonreader.theme

import androidx.compose.ui.graphics.Color

// region Primary
val PrimaryBlue = Color(0xFF135BEC)
// endregion

// region Dark Palette
val DarkBackground = Color(0xFF101622)
val DarkSurface = Color(0xFF1C2333)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
// endregion

// region Light Palette
val LightBackground = Color(0xFFF6F6F8)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF1E293B)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
// endregion

// region Syntax Highlighting
object SyntaxColors {
    object Dark {
        val key = Color(0xFF569CD6)
        val string = Color(0xFF98C379)
        val number = Color(0xFFD19A66)
        val boolean = Color(0xFFC678DD)
        val nullValue = Color(0xFFE06C75)
    }
    
    object Light {
        val key = Color(0xFF0451A5)
        val string = Color(0xFF098658)
        val number = Color(0xFFD06A16)
        val boolean = Color(0xFF7E3CB5)
        val nullValue = Color(0xFFCD3131)
    }
}
// endregion
