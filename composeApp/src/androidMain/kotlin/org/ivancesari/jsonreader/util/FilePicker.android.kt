package org.ivancesari.jsonreader.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import android.app.Activity
import android.content.Intent

private const val TAG = "FilePicker"

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onFilePicked(null)
            return@rememberLauncherForActivityResult
        }
        
        val uri = result.data?.data
        if (uri == null) {
            onFilePicked(null)
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Error taking persistable URI permission", e)
        }

        val fileInfo = resolveJsonFileInfo(uri.toString())
        if (fileInfo != null) {
            onFilePicked(
                PickedFile(
                    name = fileInfo.name,
                    sizeInBytes = fileInfo.sizeInBytes,
                    path = fileInfo.path
                )
            )
        } else {
            onFilePicked(null)
        }
    }

    return { 
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        launcher.launch(intent)
    }
}
