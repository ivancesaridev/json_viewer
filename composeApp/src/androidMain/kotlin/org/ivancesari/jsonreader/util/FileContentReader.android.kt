package org.ivancesari.jsonreader.util

import android.net.Uri

private const val TAG = "FileContentReader"

actual fun readFileContent(path: String): FileReadResult {
    val context = ContextProvider.context ?: return FileReadResult.NotReadable
    val uri = runCatching { Uri.parse(path) }.getOrNull() ?: return FileReadResult.NotReadable

    return try {
        // Check the size before opening a stream, so an oversized file is rejected without ever
        // being buffered into memory.
        val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        if (size > MAX_JSON_FILE_SIZE_BYTES) {
            return FileReadResult.TooLarge(size, MAX_JSON_FILE_SIZE_BYTES)
        }

        val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: return FileReadResult.NotReadable

        FileReadResult.Success(content)
    } catch (e: OutOfMemoryError) {
        // Providers are allowed to report an unknown size (statSize == -1), so a file can still turn
        // out to be too large once it is being read.
        Logger.e(TAG, "Ran out of memory reading file content", null)
        FileReadResult.TooLarge(sizeInBytes = -1L, limitInBytes = MAX_JSON_FILE_SIZE_BYTES)
    } catch (e: Exception) {
        Logger.e(TAG, "Error reading file content", e)
        FileReadResult.NotReadable
    }
}
