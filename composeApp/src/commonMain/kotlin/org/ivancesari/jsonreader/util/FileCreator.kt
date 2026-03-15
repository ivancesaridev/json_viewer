package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable

/**
 * Returns a launcher that opens the native document creation picker.
 * It delivers the created file's metadata via [onFileCreated].
 * You pass the desired default file name as a parameter when invoking the returned lambda.
 */
@Composable
expect fun rememberFileCreator(onFileCreated: (PickedFile?) -> Unit): (String) -> Unit
