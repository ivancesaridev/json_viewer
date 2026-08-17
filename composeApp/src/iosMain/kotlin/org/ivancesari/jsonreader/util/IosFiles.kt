package org.ivancesari.jsonreader.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/** Folder that imported documents are copied into, inside the app's Documents directory. */
internal const val IMPORTED_DIRECTORY = "Documents"

/**
 * Shared iOS file helpers.
 *
 * Documents picked through the system picker are imported into the app's own container rather than
 * being accessed in place. In-place access would require security-scoped bookmarks to survive a
 * relaunch, and every read and write would have to be wrapped in start/stop access calls. Importing
 * keeps the file model identical to a plain path, which is what the rest of the app expects.
 */
internal object IosFiles {

    /** Absolute path of the app's Documents directory. */
    fun documentsPath(): String? =
        NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String

    /** Directory that imported documents live in, created on first use. */
    fun importedDirectory(): String? {
        val documents = documentsPath() ?: return null
        val directory = "$documents/$IMPORTED_DIRECTORY"
        val manager = NSFileManager.defaultManager
        if (!manager.fileExistsAtPath(directory)) {
            manager.createDirectoryAtPath(
                path = directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        return directory
    }

    /**
     * Returns a path inside [directory] for [fileName] that is not already taken, appending a
     * counter when needed so importing the same file twice does not overwrite the first copy.
     */
    fun uniquePath(directory: String, fileName: String): String {
        val manager = NSFileManager.defaultManager
        val candidate = "$directory/$fileName"
        if (!manager.fileExistsAtPath(candidate)) return candidate

        val dotIndex = fileName.lastIndexOf('.')
        val base = if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
        val extension = if (dotIndex > 0) fileName.substring(dotIndex) else ""

        var counter = 2
        while (true) {
            val next = "$directory/$base-$counter$extension"
            if (!manager.fileExistsAtPath(next)) return next
            counter++
        }
    }

    /** Strips a `file://` prefix so the value can be handed to NSFileManager APIs. */
    fun toFileSystemPath(path: String): String =
        if (path.startsWith("file://")) path.removePrefix("file://") else path

    /** The view controller that modal pickers and share sheets should be presented from. */
    fun topViewController(): UIViewController? {
        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        val window = scene?.windows?.firstOrNull() as? UIWindow

        var controller = window?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}
