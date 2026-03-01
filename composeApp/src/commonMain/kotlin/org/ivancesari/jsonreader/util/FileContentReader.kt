package org.ivancesari.jsonreader.util

/**
 * Reads the content of a file located at [path] and returns it as a String.
 * Returns null if the file cannot be read.
 */
expect fun readFileContent(path: String): String?
