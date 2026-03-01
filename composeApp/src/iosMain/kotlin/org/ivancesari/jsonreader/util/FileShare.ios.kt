package org.ivancesari.jsonreader.util

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

actual fun shareFile(path: String) {
    try {
        val cleanPath = if (path.startsWith("file://")) path else "file://$path"
        val url = NSURL.URLWithString(cleanPath) ?: return
        
        val activityViewController = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )

        val windowScene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        val window = windowScene?.windows?.firstOrNull() as? UIWindow
        val rootViewController = window?.rootViewController
        
        // Needed for iPad
        activityViewController.popoverPresentationController?.sourceView = rootViewController?.view
        
        rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
