package org.ivancesari.jsonreader.util

import android.net.Uri
import android.provider.OpenableColumns
import org.ivancesari.jsonreader.model.JsonFileInfo

private const val TAG = "UriResolver"

actual fun resolveJsonFileInfo(path: String): JsonFileInfo? {
    val context = ContextProvider.context ?: return null
    val uri = runCatching { Uri.parse(path) }.getOrNull() ?: return null

    var name: String? = null
    var size = 0L
    var reachable = false

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                reachable = true
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0 && !cursor.isNull(nameIdx)) name = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }

        if (size <= 0L) {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                reachable = true
                size = fd.statSize.coerceAtLeast(0L)
            }
        }
    } catch (e: Exception) {
        // A revoked permission, a deleted file or an unavailable provider all land here. Report the
        // file as unresolvable rather than returning a placeholder the caller would treat as valid.
        Logger.e(TAG, "Error resolving JSON file info", e)
        return null
    }

    if (!reachable) return null

    return JsonFileInfo(
        name = name ?: uri.lastPathSegment?.substringAfterLast('/').orEmpty().ifBlank { "file.json" },
        sizeInBytes = size,
        lastOpenedTimestamp = currentTimeMillis(),
        path = path
    )
}
