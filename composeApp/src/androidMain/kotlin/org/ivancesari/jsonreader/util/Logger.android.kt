package org.ivancesari.jsonreader.util

import android.util.Log
import org.ivancesari.jsonreader.BuildConfig

/**
 * Android implementation of [Logger] that only logs in debug mode.
 */
actual object Logger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}
