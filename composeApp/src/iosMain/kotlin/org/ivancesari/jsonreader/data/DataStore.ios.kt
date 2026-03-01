package org.ivancesari.jsonreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

private lateinit var dataStore: DataStore<Preferences>

@OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)
actual fun createDataStore(): DataStore<Preferences> {
    if (!::dataStore.isInitialized) {
        dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                (requireNotNull(documentDirectory).path + "/recent_files.preferences_pb").toPath()
            }
        )
    }
    return dataStore
}
