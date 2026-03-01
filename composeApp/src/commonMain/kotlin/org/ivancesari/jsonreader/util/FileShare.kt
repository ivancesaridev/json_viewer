package org.ivancesari.jsonreader.util

/**
 * Shares the file located at [path].
 * The behavior is platform-specific (Android Intent vs iOS UIActivityViewController).
 */
expect fun shareFile(path: String)
