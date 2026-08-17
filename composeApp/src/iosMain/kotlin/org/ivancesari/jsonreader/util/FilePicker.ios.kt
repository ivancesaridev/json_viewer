package org.ivancesari.jsonreader.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController

private const val TAG = "FilePicker"

/**
 * Presents the system document picker.
 *
 * `asCopy = true` hands back a copy the app already owns, which avoids security-scoped bookmarks
 * and keeps the file readable after a relaunch. The copy is then moved into the app's Documents
 * folder so it survives the temporary directory being cleaned up.
 */
@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    val delegate = remember { DocumentPickerDelegate() }

    DisposableEffect(onFilePicked) {
        delegate.onResult = { url -> onFilePicked(url?.let { importPickedDocument(it) }) }
        onDispose { delegate.onResult = null }
    }

    return {
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeJSON),
            asCopy = true
        )
        picker.delegate = delegate
        picker.allowsMultipleSelection = false
        IosFiles.topViewController()?.presentViewController(picker, animated = true, completion = null)
            ?: onFilePicked(null)
    }
}

/** Moves the picker's temporary copy into Documents and describes the result. */
internal fun importPickedDocument(url: NSURL): PickedFile? {
    val sourcePath = url.path ?: return null
    val fileName = url.lastPathComponent ?: "file.json"
    val directory = IosFiles.importedDirectory() ?: return null
    val destination = IosFiles.uniquePath(directory, fileName)

    val manager = platform.Foundation.NSFileManager.defaultManager
    val moved = manager.moveItemAtPath(sourcePath, destination, null) ||
        manager.copyItemAtPath(sourcePath, destination, null)

    if (!moved) {
        Logger.e(TAG, "Could not import picked document into app storage", null)
        return null
    }

    val info = resolveJsonFileInfo(destination) ?: return null
    return PickedFile(name = info.name, sizeInBytes = info.sizeInBytes, path = info.path)
}

/**
 * Retained across recompositions: UIKit holds its delegate weakly, so a delegate created inline
 * would be deallocated before the picker could call back.
 */
internal class DocumentPickerDelegate : NSObject(), UIDocumentPickerDelegateProtocol {

    var onResult: ((NSURL?) -> Unit)? = null

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        onResult?.invoke(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult?.invoke(null)
    }
}
