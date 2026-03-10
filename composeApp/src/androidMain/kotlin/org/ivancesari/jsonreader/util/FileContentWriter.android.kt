package org.ivancesari.jsonreader.util

import android.net.Uri
import org.ivancesari.jsonreader.util.ContextProvider

private const val TAG = "FileContentWriter"

actual fun saveFileContent(path: String, content: String): Boolean {
    return try {
        val uri = Uri.parse(path)
        val context = ContextProvider.context
        context?.contentResolver?.openOutputStream(uri, "wt")?.use { outputStream ->
            outputStream.bufferedWriter().use { it.write(content) }
        }
        true
    } catch (e: Exception) {
        Logger.e(TAG, "Error saving file content", e)
        false
    }
}
