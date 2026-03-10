package org.ivancesari.jsonreader.feature.jsondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.ivancesari.jsonreader.util.readFileContent

sealed class JsonDetailState {
    object Loading : JsonDetailState()
    data class Error(val message: String) : JsonDetailState()
    data class Success(
        val fileName: String,
        val jsonString: String,
        val jsonTree: JsonElement,
        val searchQuery: String = "",
        val expandedPaths: Set<String> = emptySet(),
        val searchResults: List<String> = emptyList(), // Store paths to matching nodes
        val currentSearchIndex: Int = -1
    ) : JsonDetailState()
}

class JsonDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<JsonDetailState>(JsonDetailState.Loading)
    val uiState: StateFlow<JsonDetailState> = _uiState.asStateFlow()

    fun loadFile(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = JsonDetailState.Loading
                
                val content = readFileContent(filePath)
                if (content == null) {
                    _uiState.value = JsonDetailState.Error("Unable to read file content")
                    return@launch
                }
                
                // Parse tree
                val jsonTree = Json.parseToJsonElement(content)

                // Extract filename
                val fileName = filePath.substringAfterLast('/').substringAfterLast('%')
                
                _uiState.value = JsonDetailState.Success(
                    fileName = fileName,
                    jsonString = content,
                    jsonTree = jsonTree,
                    expandedPaths = setOf("root") // start with root expanded
                )
            } catch (e: Exception) {
                _uiState.value = JsonDetailState.Error(e.message ?: "Unknown error parsing JSON")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is JsonDetailState.Success) {
            _uiState.update { 
                currentState.copy(
                    searchQuery = query,
                    // Basic exact match for now - expand to deep search later
                )
            }
        }
    }

    fun toggleNode(path: String) {
        val currentState = _uiState.value
        if (currentState is JsonDetailState.Success) {
            val currentExpanded = currentState.expandedPaths.toMutableSet()
            if (currentExpanded.contains(path)) {
                currentExpanded.remove(path)
            } else {
                currentExpanded.add(path)
            }
            _uiState.update { currentState.copy(expandedPaths = currentExpanded) }
        }
    }
}
