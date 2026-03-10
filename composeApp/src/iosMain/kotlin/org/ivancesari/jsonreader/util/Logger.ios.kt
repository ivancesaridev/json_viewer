package org.ivancesari.jsonreader.util

import platform.Foundation.NSLog
import kotlin.native.Platform

/**
 * iOS implementation of [Logger] that logs only in debug mode.
 */
actual object Logger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (Platform.isDebugBinary) {
            val fullMessage = if (throwable != null) {
                "[$tag] $message: ${throwable.message}"
            } else {
                "[$tag] $message"
            }
            NSLog("%@", fullMessage)
        }
    }
}
