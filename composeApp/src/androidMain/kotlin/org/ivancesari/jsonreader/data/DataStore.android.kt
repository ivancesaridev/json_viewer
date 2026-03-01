package org.ivancesari.jsonreader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import org.ivancesari.jsonreader.util.ContextProvider

private lateinit var dataStore: DataStore<Preferences>

actual fun createDataStore(): DataStore<Preferences> {
    if (!::dataStore.isInitialized) {
        val context = ContextProvider.context ?: throw IllegalStateException("Context not initialized")
        dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { context.filesDir.resolve("recent_files.preferences_pb").absolutePath.toPath() }
        )
    }
    return dataStore
}
