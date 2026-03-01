package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    // iOS implementation not yet available.
    return { onFilePicked(null) }
}
