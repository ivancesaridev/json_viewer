package org.ivancesari.jsonreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.ivancesari.jsonreader.model.JsonFileInfo

class RecentFilesRepository(private val dataStore: DataStore<Preferences>) {

    private val RECENT_FILES_KEY = stringPreferencesKey("recent_files")

    val recentFilesFlow: Flow<List<JsonFileInfo>> = dataStore.data.map { preferences ->
        val jsonString = preferences[RECENT_FILES_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<JsonFileInfo>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRecentFiles(files: List<JsonFileInfo>) {
        dataStore.edit { preferences ->
            val jsonString = Json.encodeToString(files)
            preferences[RECENT_FILES_KEY] = jsonString
        }
    }
}

expect fun createDataStore(): DataStore<Preferences>
