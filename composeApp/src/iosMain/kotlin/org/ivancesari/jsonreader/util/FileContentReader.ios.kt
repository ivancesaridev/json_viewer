package org.ivancesari.jsonreader.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual fun readFileContent(path: String): String? {
    return try {
        // Path might come as file://..., removing the prefix for NSFileManager if present
        val cleanPath = if (path.startsWith("file://")) path.removePrefix("file://") else path
        
        val nsString = NSString.stringWithContentsOfFile(
            path = cleanPath,
            encoding = NSUTF8StringEncoding,
            error = null
        )
        nsString
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
