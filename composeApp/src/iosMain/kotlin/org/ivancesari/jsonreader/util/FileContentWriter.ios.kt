package org.ivancesari.jsonreader.util

import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile

private const val TAG = "FileContentWriter"

actual fun saveFileContent(path: String, content: String): Boolean {
    val filePath = IosFiles.toFileSystemPath(path)

    // writeToFile reports failure through its return value; the previous implementation returned it
    // directly but wrapped the call in a catch that could never fire, hiding real write errors.
    val written = (content as NSString).writeToFile(
        path = filePath,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )

    if (!written) Logger.e(TAG, "Could not write $filePath", null)
    return written
}
