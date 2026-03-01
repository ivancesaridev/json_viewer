package org.ivancesari.jsonreader.util

/**
 * Saves the [content] to a file located at [path].
 * Returns true if the file was saved successfully, false otherwise.
 */
expect fun saveFileContent(path: String, content: String): Boolean
