package org.ivancesari.jsonreader.util

import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

private const val TAG = "FileContentReader"

actual fun readFileContent(path: String): FileReadResult {
    val filePath = IosFiles.toFileSystemPath(path)
    val manager = NSFileManager.defaultManager

    if (!manager.fileExistsAtPath(filePath)) return FileReadResult.NotReadable

    val attributes = manager.attributesOfItemAtPath(filePath, null)
    val size = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
    if (size > MAX_JSON_FILE_SIZE_BYTES) {
        return FileReadResult.TooLarge(size, MAX_JSON_FILE_SIZE_BYTES)
    }

    val content = NSString.stringWithContentsOfFile(
        path = filePath,
        encoding = NSUTF8StringEncoding,
        error = null
    )

    return if (content == null) {
        Logger.e(TAG, "Could not read $filePath as UTF-8", null)
        FileReadResult.NotReadable
    } else {
        FileReadResult.Success(content)
    }
}
