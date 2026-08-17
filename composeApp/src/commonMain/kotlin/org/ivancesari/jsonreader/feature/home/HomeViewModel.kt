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

class HomeViewModel(
    private val repository: RecentFilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        repository.recentFilesFlow
            .onEach { files ->
                _uiState.update { it.copy(recentFiles = files, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onFilePicked(file: JsonFileInfo) {
        viewModelScope.launch { repository.addRecentFile(file) }
    }

    fun onRemoveRecentFile(path: String) {
        viewModelScope.launch { repository.removeRecentFile(path) }
    }
}
