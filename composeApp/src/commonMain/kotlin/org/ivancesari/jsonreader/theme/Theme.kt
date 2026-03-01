package org.ivancesari.jsonreader.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

data class SyntaxColorPalette(
    val key: Color,
    val string: Color,
    val number: Color,
    val boolean: Color,
    val nullValue: Color,
    val punctuation: Color
)

val LocalSyntaxColors = staticCompositionLocalOf<SyntaxColorPalette> {
    error("No SyntaxColorPalette provided")
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = DarkOnBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = DarkOnBackground,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun JsonReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val syntaxColors = if (darkTheme) {
        SyntaxColorPalette(
            key = SyntaxColors.Dark.key,
            string = SyntaxColors.Dark.string,
            number = SyntaxColors.Dark.number,
            boolean = SyntaxColors.Dark.boolean,
            nullValue = SyntaxColors.Dark.nullValue,
            punctuation = colorScheme.onSurfaceVariant
        )
    } else {
        SyntaxColorPalette(
            key = SyntaxColors.Light.key,
            string = SyntaxColors.Light.string,
            number = SyntaxColors.Light.number,
            boolean = SyntaxColors.Light.boolean,
            nullValue = SyntaxColors.Light.nullValue,
            punctuation = colorScheme.onSurfaceVariant
        )
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSyntaxColors provides syntaxColors
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
}
