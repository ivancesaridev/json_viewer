package org.ivancesari.jsonreader.util

import android.net.Uri
import android.provider.OpenableColumns
import org.ivancesari.jsonreader.model.JsonFileInfo

actual fun resolveJsonFileInfo(path: String): JsonFileInfo? {
    val context = ContextProvider.context ?: return null
    val uri = try { Uri.parse(path) } catch (e: Exception) { return null }
    
    var name = "unknown.json"
    var size = 0L

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: "unknown.json"
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                    size = cursor.getLong(sizeIdx)
                }
            }
        }

        if (size <= 0L) {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                size = fd.statSize
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return JsonFileInfo(
        name = name,
        sizeInBytes = size,
        lastOpenedTimestamp = currentTimeMillis(),
        path = path
    )
}
