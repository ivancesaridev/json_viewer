package org.ivancesari.jsonreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.ivancesari.jsonreader.util.ContextProvider

private const val STORE_FILE_NAME = "recent_files.preferences_pb"

/**
 * Created once per process. DataStore throws if two instances are opened over the same file, so the
 * instance is built lazily and the lazy itself guards against a race between callers.
 */
private val dataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            ContextProvider.requireContext().filesDir.resolve(STORE_FILE_NAME).absolutePath.toPath()
        }
    )
}

actual fun createDataStore(): DataStore<Preferences> = dataStore
