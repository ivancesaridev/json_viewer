package org.ivancesari.jsonreader.util

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

private const val TAG = "FileShare"

actual suspend fun shareFile(path: String): Boolean {
    val filePath = IosFiles.toFileSystemPath(path)
    if (!NSFileManager.defaultManager.fileExistsAtPath(filePath)) return false

    // fileURLWithPath, not URLWithString: the latter silently returns null for paths containing
    // spaces or other characters that are not valid in a URL without escaping.
    val url = NSURL.fileURLWithPath(filePath)

    val controller = IosFiles.topViewController() ?: run {
        Logger.e(TAG, "No view controller available to present the share sheet", null)
        return false
    }

    val activityViewController = UIActivityViewController(
        activityItems = listOf(url),
        applicationActivities = null
    )
    // Required on iPad, where the sheet is presented as a popover anchored to a source view.
    activityViewController.popoverPresentationController?.sourceView = controller.view

    controller.presentViewController(activityViewController, animated = true, completion = null)
    return true
}
