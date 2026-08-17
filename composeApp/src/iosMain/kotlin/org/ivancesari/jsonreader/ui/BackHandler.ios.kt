package org.ivancesari.jsonreader.ui

import androidx.compose.runtime.Composable

/**
 * iOS has no system back action to intercept — navigation back happens through the app's own
 * controls, which already run the unsaved-changes check.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
