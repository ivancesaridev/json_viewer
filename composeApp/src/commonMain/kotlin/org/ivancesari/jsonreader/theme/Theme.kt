package org.ivancesari.jsonreader.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SyntaxColorPalette(
    val key: Color,
    val string: Color,
    val number: Color,
    val boolean: Color,
    val nullValue: Color,
    val punctuation: Color,
    /** Used for the "Object (3)" / "Array (12)" badge on collapsed branches. */
    val summary: Color
)

val LocalSyntaxColors = staticCompositionLocalOf<SyntaxColorPalette> {
    error("No SyntaxColorPalette provided")
}

val LocalEditorColors = staticCompositionLocalOf<EditorColorSet> {
    error("No EditorColorSet provided")
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
    onError = Color.White
)

@Composable
fun JsonReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val syntax = if (darkTheme) SyntaxColors.Dark else SyntaxColors.Light

    val syntaxColors = SyntaxColorPalette(
        key = syntax.key,
        string = syntax.string,
        number = syntax.number,
        boolean = syntax.boolean,
        nullValue = syntax.nullValue,
        punctuation = colorScheme.onSurfaceVariant,
        summary = colorScheme.onSurfaceVariant
    )

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSyntaxColors provides syntaxColors,
        LocalEditorColors provides if (darkTheme) EditorColors.Dark else EditorColors.Light
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object JsonReaderTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val editor: EditorColorSet
        @Composable
        @ReadOnlyComposable
        get() = LocalEditorColors.current
}
