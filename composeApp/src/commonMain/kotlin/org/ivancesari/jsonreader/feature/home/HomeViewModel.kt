package org.ivancesari.jsonreader.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ivancesari.jsonreader.data.RecentFilesRepository
import org.ivancesari.jsonreader.model.JsonFileInfo
import org.ivancesari.jsonreader.util.currentTimeMillis

class HomeViewModel(
    private val repository: RecentFilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentFiles()
    }

    private fun loadRecentFiles() {
        repository.recentFilesFlow.onEach { files ->
            _uiState.update { it.copy(recentFiles = files) }
        }.launchIn(viewModelScope)
    }

    fun onBrowseStorage() {
        // Handled in the UI via FileKit picker
    }

    fun onFilePicked(file: JsonFileInfo) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedList = (listOf(file) + currentState.recentFiles.filter { it.path != file.path }).take(10)
            repository.saveRecentFiles(updatedList)
        }
    }

    fun onFileSelected(file: JsonFileInfo) {
        // Will navigate to file viewer in a future iteration
    }
}
