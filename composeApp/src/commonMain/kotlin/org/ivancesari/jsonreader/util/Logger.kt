package org.ivancesari.jsonreader.util

/**
 * A simple logger that provides platform-specific logging.
 */
expect object Logger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
