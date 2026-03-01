package org.ivancesari.jsonreader.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual fun saveFileContent(path: String, content: String): Boolean {
    return try {
        val cleanPath = if (path.startsWith("file://")) path.removePrefix("file://") else path
        val nsString = content as NSString
        nsString.writeToFile(
            path = cleanPath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
