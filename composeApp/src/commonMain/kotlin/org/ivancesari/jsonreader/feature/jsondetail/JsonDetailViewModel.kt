package org.ivancesari.jsonreader.feature.jsondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.ivancesari.jsonreader.json.JsonTreeRow
import org.ivancesari.jsonreader.json.ROOT_PATH
import org.ivancesari.jsonreader.json.flattenJson
import org.ivancesari.jsonreader.json.searchJson
import org.ivancesari.jsonreader.util.FileReadResult
import org.ivancesari.jsonreader.util.Logger
import org.ivancesari.jsonreader.util.readFileContent

private const val TAG = "JsonDetailViewModel"
private const val SEARCH_DEBOUNCE_MILLIS = 220L

/** Why a file could not be shown, in terms the UI can turn into a sentence for the user. */
sealed interface JsonDetailError {
    data object NotReadable : JsonDetailError
    data class TooLarge(val limitInBytes: Long) : JsonDetailError
    data class InvalidJson(val detail: String?) : JsonDetailError
}

sealed interface JsonDetailState {

    data object Loading : JsonDetailState

    data class Error(val error: JsonDetailError) : JsonDetailState

    data class Success(
        val jsonString: String,
        val rows: List<JsonTreeRow> = emptyList(),
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        val matchedPaths: Set<String> = emptySet(),
        val matches: List<String> = emptyList(),
        val currentMatchIndex: Int = -1
    ) : JsonDetailState {

        /** Path of the match currently focused, or null when there is no active match. */
        val focusedPath: String?
            get() = matches.getOrNull(currentMatchIndex)
    }
}

class JsonDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<JsonDetailState>(JsonDetailState.Loading)
    val uiState: StateFlow<JsonDetailState> = _uiState.asStateFlow()

    /** Index into [JsonDetailState.Success.rows] that the list should scroll to, consumed once. */
    private val _scrollToRow = MutableStateFlow<Int?>(null)
    val scrollToRow: StateFlow<Int?> = _scrollToRow.asStateFlow()

    private var tree: JsonElement? = null
    private var expandedPaths: Set<String> = setOf(ROOT_PATH)
    private var searchJob: Job? = null
    private var loadedPath: String? = null

    fun loadFile(filePath: String) {
        // LaunchedEffect can re-run across configuration changes while the view model survives;
        // reloading would throw away the user's expanded branches and scroll position.
        if (loadedPath == filePath && _uiState.value !is JsonDetailState.Loading) return
        loadedPath = filePath

        viewModelScope.launch {
            _uiState.value = JsonDetailState.Loading

            when (val result = withContext(Dispatchers.IO) { readFileContent(filePath) }) {
                is FileReadResult.NotReadable ->
                    _uiState.value = JsonDetailState.Error(JsonDetailError.NotReadable)

                is FileReadResult.TooLarge ->
                    _uiState.value = JsonDetailState.Error(JsonDetailError.TooLarge(result.limitInBytes))

                is FileReadResult.Success -> {
                    val parsed = withContext(Dispatchers.Default) {
                        runCatching { Json.parseToJsonElement(result.content) }
                    }

                    parsed.fold(
                        onSuccess = { element ->
                            tree = element
                            expandedPaths = setOf(ROOT_PATH)
                            _uiState.value = JsonDetailState.Success(
                                jsonString = result.content,
                                rows = withContext(Dispatchers.Default) {
                                    flattenJson(element, expandedPaths)
                                }
                            )
                        },
                        onFailure = { throwable ->
                            Logger.e(TAG, "Error parsing JSON", throwable)
                            _uiState.value = JsonDetailState.Error(
                                JsonDetailError.InvalidJson(throwable.message?.takeIf { it.isNotBlank() })
                            )
                        }
                    )
                }
            }
        }
    }

    fun toggleNode(path: String) {
        val element = tree ?: return
        expandedPaths = if (path in expandedPaths) expandedPaths - path else expandedPaths + path
        reflow(element)
    }

    fun updateSearchQuery(query: String) {
        val current = _uiState.value as? JsonDetailState.Success ?: return
        _uiState.value = current.copy(searchQuery = query, isSearching = query.isNotBlank())

        searchJob?.cancel()
        val element = tree ?: return

        if (query.isBlank()) {
            _uiState.update { state ->
                (state as? JsonDetailState.Success)?.copy(
                    isSearching = false,
                    matchedPaths = emptySet(),
                    matches = emptyList(),
                    currentMatchIndex = -1
                ) ?: state
            }
            return
        }

        searchJob = viewModelScope.launch {
            // Debounced so a fast typist does not trigger a full document walk per keystroke.
            delay(SEARCH_DEBOUNCE_MILLIS)

            val result = withContext(Dispatchers.Default) { searchJson(element, query) }
            expandedPaths = expandedPaths + result.pathsToExpand

            val rows = withContext(Dispatchers.Default) { flattenJson(element, expandedPaths) }
            val firstMatchIndex = if (result.matches.isEmpty()) -1 else 0

            _uiState.update { state ->
                (state as? JsonDetailState.Success)?.copy(
                    rows = rows,
                    isSearching = false,
                    matchedPaths = result.matchedPaths,
                    matches = result.matches,
                    currentMatchIndex = firstMatchIndex
                ) ?: state
            }

            if (firstMatchIndex >= 0) requestScrollToMatch(rows, result.matches[0])
        }
    }

    fun goToNextMatch() = stepMatch(1)

    fun goToPreviousMatch() = stepMatch(-1)

    private fun stepMatch(delta: Int) {
        val current = _uiState.value as? JsonDetailState.Success ?: return
        if (current.matches.isEmpty()) return

        val size = current.matches.size
        val next = ((current.currentMatchIndex + delta) % size + size) % size
        _uiState.value = current.copy(currentMatchIndex = next)
        requestScrollToMatch(current.rows, current.matches[next])
    }

    fun onScrollHandled() {
        _scrollToRow.value = null
    }

    private fun requestScrollToMatch(rows: List<JsonTreeRow>, path: String) {
        val index = rows.indexOfFirst { it !is JsonTreeRow.Closing && it.path == path }
        if (index >= 0) _scrollToRow.value = index
    }

    private fun reflow(element: JsonElement) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.Default) { flattenJson(element, expandedPaths) }
            _uiState.update { state ->
                (state as? JsonDetailState.Success)?.copy(rows = rows) ?: state
            }
        }
    }
}
