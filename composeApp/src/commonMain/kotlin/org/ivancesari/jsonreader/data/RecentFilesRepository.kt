package org.ivancesari.jsonreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ivancesari.jsonreader.model.JsonFileInfo
import org.ivancesari.jsonreader.util.Logger

private const val TAG = "RecentFilesRepository"

/** Number of entries kept in the recent-files list. */
const val MAX_RECENT_FILES = 10

class RecentFilesRepository(private val dataStore: DataStore<Preferences>) {

    private val recentFilesKey = stringPreferencesKey("recent_files")
    private val json = Json { ignoreUnknownKeys = true }

    val recentFilesFlow: Flow<List<JsonFileInfo>> = dataStore.data
        .catch { throwable ->
            // A corrupt or unreadable store should degrade to an empty list rather than crash the
            // home screen; DataStore surfaces those as exceptions on the flow itself.
            Logger.e(TAG, "Error reading recent files", throwable)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val stored = preferences[recentFilesKey] ?: return@map emptyList()
            try {
                json.decodeFromString<List<JsonFileInfo>>(stored)
            } catch (e: Exception) {
                Logger.e(TAG, "Discarding malformed recent files entry", e)
                emptyList()
            }
        }

    /** Moves [file] to the top of the list, de-duplicating by path and trimming to the maximum. */
    suspend fun addRecentFile(file: JsonFileInfo) = update { current ->
        (listOf(file) + current.filter { it.path != file.path }).take(MAX_RECENT_FILES)
    }

    /** Removes the entry with the given [path], if present. */
    suspend fun removeRecentFile(path: String) = update { current ->
        current.filterNot { it.path == path }
    }

    private suspend fun update(transform: (List<JsonFileInfo>) -> List<JsonFileInfo>) {
        try {
            dataStore.edit { preferences ->
                val current = preferences[recentFilesKey]?.let { stored ->
                    runCatching { json.decodeFromString<List<JsonFileInfo>>(stored) }.getOrDefault(emptyList())
                } ?: emptyList()

                preferences[recentFilesKey] = json.encodeToString(transform(current))
            }
        } catch (e: Exception) {
            // Losing a recent-files entry is not worth crashing over.
            Logger.e(TAG, "Error writing recent files", e)
        }
    }
}

expect fun createDataStore(): DataStore<Preferences>
