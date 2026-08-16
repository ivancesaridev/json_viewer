package org.ivancesari.jsonreader.util

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "FileShare"
private const val SHARE_DIRECTORY = "shared"

actual suspend fun shareFile(path: String): Boolean {
    val context = ContextProvider.context ?: return false
    val sourceUri = runCatching { Uri.parse(path) }.getOrNull() ?: return false

    // A content:// URI obtained through the document picker is granted to this app only. Passing it
    // straight to another app makes the receiver fail with a SecurityException, so the file is
    // staged in our own cache and shared through our FileProvider, whose grant we can extend.
    val staged = withContext(Dispatchers.IO) {
        runCatching {
            val name = resolveJsonFileInfo(path)?.name?.takeIf { it.isNotBlank() } ?: "shared.json"
            val directory = File(context.cacheDir, SHARE_DIRECTORY).apply { mkdirs() }
            // Only ever keep the most recent staged copy around.
            directory.listFiles()?.forEach { it.delete() }

            val destination = File(directory, name.substringAfterLast('/'))
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null

            destination
        }.onFailure { Logger.e(TAG, "Error staging file for sharing", it) }
            .getOrNull()
    } ?: return false

    return try {
        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            staged
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
        true
    } catch (e: Exception) {
        Logger.e(TAG, "Error sharing file", e)
        false
    }
}
