package org.ivancesari.jsonreader.util

/**
 * Largest file the app will load into memory.
 *
 * Reading a file costs roughly three times its size in memory: the raw string, the parsed
 * [kotlinx.serialization.json.JsonElement] tree, and the copy the UI state holds. Eight megabytes
 * is comfortably above any hand-written JSON while staying well inside a typical heap.
 */
const val MAX_JSON_FILE_SIZE_BYTES: Long = 8L * 1024 * 1024

/** Outcome of reading a file, so callers can tell the user *why* a file did not open. */
sealed interface FileReadResult {

    /** The file was read successfully. */
    data class Success(val content: String) : FileReadResult

    /** The file exists but is bigger than [MAX_JSON_FILE_SIZE_BYTES]. */
    data class TooLarge(val sizeInBytes: Long, val limitInBytes: Long) : FileReadResult

    /**
     * The file could not be opened: it was moved or deleted, the permission grant was revoked, or
     * the providing app is gone.
     */
    data object NotReadable : FileReadResult
}

/**
 * Reads the content of the file at [path].
 *
 * Refuses files above [MAX_JSON_FILE_SIZE_BYTES] instead of attempting a read that would end in an
 * OutOfMemoryError.
 */
expect fun readFileContent(path: String): FileReadResult
