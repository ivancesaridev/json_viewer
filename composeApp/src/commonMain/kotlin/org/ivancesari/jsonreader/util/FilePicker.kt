package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable

/**
 * A pick result containing file metadata.
 */
data class PickedFile(
    val name: String,
    val sizeInBytes: Long,
    val path: String
)

/**
 * Returns a launcher that opens the native document picker and delivers result via [onFilePicked].
 */
@Composable
expect fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit
