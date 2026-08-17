package org.ivancesari.jsonreader.ui

import androidx.compose.runtime.Composable

/**
 * Intercepts the platform's "go back" gesture while [enabled].
 *
 * Needed so the editor can ask about unsaved changes when the user swipes or presses back, not only
 * when they tap Cancel. On iOS there is no system-wide back action to intercept, so the actual
 * implementation there is a no-op.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
