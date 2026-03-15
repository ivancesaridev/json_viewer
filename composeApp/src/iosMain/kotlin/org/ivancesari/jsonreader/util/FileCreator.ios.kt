package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileCreator(onFileCreated: (PickedFile?) -> Unit): (String) -> Unit {
    // iOS implementation will come later. For now, trigger null callback.
    return { _ -> onFileCreated(null) }
}
