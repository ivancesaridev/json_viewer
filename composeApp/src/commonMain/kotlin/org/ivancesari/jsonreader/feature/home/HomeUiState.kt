package org.ivancesari.jsonreader.feature.home

import org.ivancesari.jsonreader.model.JsonFileInfo

data class HomeUiState(
    val recentFiles: List<JsonFileInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
