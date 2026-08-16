package org.ivancesari.jsonreader.util

import android.net.Uri

private const val TAG = "FileContentWriter"

actual fun saveFileContent(path: String, content: String): Boolean {
    val context = ContextProvider.context ?: return false
    val uri = runCatching { Uri.parse(path) }.getOrNull() ?: return false

    return try {
        val stream = context.contentResolver.openOutputStream(uri, "wt")
        if (stream == null) {
            // A null stream means the provider refused the write. Previously this path still
            // reported success, so the user was told the file had been saved when it had not.
            Logger.e(TAG, "Provider returned no output stream for $uri", null)
            return false
        }
        stream.use { outputStream ->
            outputStream.bufferedWriter().use { it.write(content) }
        }
        true
    } catch (e: Exception) {
        Logger.e(TAG, "Error saving file content", e)
        false
    }
}
