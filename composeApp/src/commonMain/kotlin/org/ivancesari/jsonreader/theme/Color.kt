package org.ivancesari.jsonreader.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// region Primary
val PrimaryBlue = Color(0xFF135BEC)
// endregion

// region Dark palette
val DarkBackground = Color(0xFF101622)
val DarkSurface = Color(0xFF1C2333)
val DarkSurfaceVariant = Color(0xFF243049)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)
val DarkError = Color(0xFFFF8A80)
// endregion

// region Light palette
val LightBackground = Color(0xFFF6F6F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE7EAF0)
val LightOnBackground = Color(0xFF1E293B)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFCBD5E1)
val LightError = Color(0xFFB3261E)
// endregion

// region Syntax highlighting
@Immutable
data class SyntaxColorSet(
    val key: Color,
    val string: Color,
    val number: Color,
    val boolean: Color,
    val nullValue: Color
)

object SyntaxColors {
    val Dark = SyntaxColorSet(
        key = Color(0xFF569CD6),
        string = Color(0xFF98C379),
        number = Color(0xFFD19A66),
        boolean = Color(0xFFC678DD),
        nullValue = Color(0xFFE06C75)
    )

    val Light = SyntaxColorSet(
        key = Color(0xFF0451A5),
        string = Color(0xFF098658),
        number = Color(0xFFD06A16),
        boolean = Color(0xFF7E3CB5),
        nullValue = Color(0xFFCD3131)
    )
}
// endregion

// region Editor
/**
 * Colours for the JSON editor.
 *
 * The editor previously hard-coded a dark slate palette, which meant it stayed dark even when the
 * rest of the app was in light mode. Both variants are defined here and picked by the theme.
 */
@Immutable
data class EditorColorSet(
    val background: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val divider: Color,
    val text: Color,
    val keyBackground: Color,
    val keyContent: Color
)

object EditorColors {
    val Dark = EditorColorSet(
        background = Color(0xFF0F172A),
        gutterBackground = Color(0xFF0F172A),
        gutterText = Color(0xFF64748B),
        divider = Color(0xFF1E293B),
        text = Color(0xFFF1F5F9),
        keyBackground = Color(0xFF1E293B),
        keyContent = Color(0xFFF1F5F9)
    )

    val Light = EditorColorSet(
        background = Color(0xFFFFFFFF),
        gutterBackground = Color(0xFFF1F5F9),
        gutterText = Color(0xFF94A3B8),
        divider = Color(0xFFE2E8F0),
        text = Color(0xFF1E293B),
        keyBackground = Color(0xFFEDF1F7),
        keyContent = Color(0xFF1E293B)
    )
}
// endregion
