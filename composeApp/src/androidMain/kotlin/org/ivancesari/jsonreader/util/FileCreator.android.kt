package org.ivancesari.jsonreader.util

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private const val TAG = "FileCreator"

@Composable
actual fun rememberFileCreator(onFileCreated: (PickedFile?) -> Unit): (String) -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onFileCreated(null)
            return@rememberLauncherForActivityResult
        }

        val uri = result.data?.data
        if (uri == null) {
            onFileCreated(null)
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Error taking persistable URI permission", e)
        }

        val fileInfo = resolveJsonFileInfo(uri.toString())
        if (fileInfo != null) {
            onFileCreated(
                PickedFile(
                    name = fileInfo.name,
                    sizeInBytes = fileInfo.sizeInBytes,
                    path = fileInfo.path
                )
            )
        } else {
            onFileCreated(null)
        }
    }

    return { defaultName ->
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, defaultName)
        }
        launcher.launch(intent)
    }
}
