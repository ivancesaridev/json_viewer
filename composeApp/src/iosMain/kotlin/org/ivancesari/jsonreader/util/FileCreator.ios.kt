package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable
import platform.Foundation.NSFileManager

private const val TAG = "FileCreator"

/**
 * Creates a new document inside the app's Documents folder.
 *
 * iOS has no "save as" picker equivalent to Android's `ACTION_CREATE_DOCUMENT` that can hand back a
 * writable location before any content exists. The file is therefore created in the app's own
 * storage — where it is visible in Files, since the app declares
 * `UISupportsDocumentBrowser`/`LSSupportsOpeningDocumentsInPlace` — and the user can move it
 * elsewhere from the share sheet.
 */
@Composable
actual fun rememberFileCreator(onFileCreated: (PickedFile?) -> Unit): (String) -> Unit = { defaultName ->
    val fileName = defaultName.ifBlank { "untitled.json" }
        .let { if (it.endsWith(".json", ignoreCase = true)) it else "$it.json" }

    val directory = IosFiles.importedDirectory()
    if (directory == null) {
        Logger.e(TAG, "Documents directory unavailable", null)
        onFileCreated(null)
    } else {
        val path = IosFiles.uniquePath(directory, fileName)
        val created = NSFileManager.defaultManager.createFileAtPath(
            path = path,
            contents = null,
            attributes = null
        )

        if (created) {
            onFileCreated(PickedFile(name = path.substringAfterLast('/'), sizeInBytes = 0L, path = path))
        } else {
            Logger.e(TAG, "Could not create file at $path", null)
            onFileCreated(null)
        }
    }
}
