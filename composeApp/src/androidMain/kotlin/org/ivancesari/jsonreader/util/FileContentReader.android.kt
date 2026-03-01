package org.ivancesari.jsonreader.util

import android.net.Uri
import org.ivancesari.jsonreader.util.ContextProvider

actual fun readFileContent(path: String): String? {
    return try {
        val uri = Uri.parse(path)
        val context = ContextProvider.context
        context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
