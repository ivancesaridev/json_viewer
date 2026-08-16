package org.ivancesari.jsonreader.util

import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import org.ivancesari.jsonreader.model.JsonFileInfo

private const val TAG = "UriResolver"

actual fun resolveJsonFileInfo(path: String): JsonFileInfo? {
    val filePath = IosFiles.toFileSystemPath(path)
    val manager = NSFileManager.defaultManager

    if (!manager.fileExistsAtPath(filePath)) {
        // The file was deleted, or a recent-files entry points at something that no longer exists.
        return null
    }

    val attributes = manager.attributesOfItemAtPath(filePath, null)
    val size = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L

    return JsonFileInfo(
        name = filePath.substringAfterLast('/').ifBlank { "file.json" },
        sizeInBytes = size,
        lastOpenedTimestamp = currentTimeMillis(),
        path = filePath
    )
}
