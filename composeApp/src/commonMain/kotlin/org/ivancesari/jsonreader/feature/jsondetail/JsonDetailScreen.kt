package org.ivancesari.jsonreader.feature.jsondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.action_back
import org.ivancesari.jsonreader.resources.action_clear_search
import org.ivancesari.jsonreader.resources.action_copy
import org.ivancesari.jsonreader.resources.action_edit
import org.ivancesari.jsonreader.resources.action_next_match
import org.ivancesari.jsonreader.resources.action_previous_match
import org.ivancesari.jsonreader.resources.action_search
import org.ivancesari.jsonreader.resources.action_share
import org.ivancesari.jsonreader.resources.copied_to_clipboard
import org.ivancesari.jsonreader.resources.error_file_too_large
import org.ivancesari.jsonreader.resources.error_invalid_json
import org.ivancesari.jsonreader.resources.error_invalid_json_detail
import org.ivancesari.jsonreader.resources.error_share_failed
import org.ivancesari.jsonreader.resources.error_unable_to_read
import org.ivancesari.jsonreader.resources.ic_arrow_back
import org.ivancesari.jsonreader.resources.ic_copy
import org.ivancesari.jsonreader.resources.ic_edit
import org.ivancesari.jsonreader.resources.ic_search
import org.ivancesari.jsonreader.resources.ic_share
import org.ivancesari.jsonreader.resources.search_match_position
import org.ivancesari.jsonreader.resources.search_no_results
import org.ivancesari.jsonreader.resources.search_placeholder
import org.ivancesari.jsonreader.ui.components.JsonTreeViewer
import org.ivancesari.jsonreader.ui.format.formatFileSize
import org.ivancesari.jsonreader.util.shareFile
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun JsonDetailScreen(
    filePath: String,
    fileName: String,
    fileSize: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: JsonDetailViewModel = viewModel { JsonDetailViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val scrollToRow by viewModel.scrollToRow.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchVisible by remember { mutableStateOf(false) }

    val copiedMessage = stringResource(Res.string.copied_to_clipboard)
    val shareFailedMessage = stringResource(Res.string.error_share_failed)

    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath)
    }

    LaunchedEffect(scrollToRow) {
        val index = scrollToRow ?: return@LaunchedEffect
        listState.animateScrollToItem(index)
        viewModel.onScrollHandled()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is JsonDetailState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is JsonDetailState.Error -> {
                    DetailHeader(
                        fileName = fileName,
                        fileSize = fileSize,
                        onNavigateBack = onNavigateBack,
                        onToggleSearch = null,
                        onCopy = null
                    )
                    ErrorContent(
                        error = state.error,
                        modifier = Modifier.weight(1f)
                    )
                    // Invalid JSON is still editable, so keep a route into the editor.
                    if (state.error is JsonDetailError.InvalidJson) {
                        BottomActions(
                            onEdit = { onNavigateToEdit(filePath, fileName) },
                            onShare = null
                        )
                    }
                }

                is JsonDetailState.Success -> {
                    DetailHeader(
                        fileName = fileName,
                        fileSize = fileSize,
                        onNavigateBack = onNavigateBack,
                        onToggleSearch = {
                            searchVisible = !searchVisible
                            if (!searchVisible) {
                                viewModel.updateSearchQuery("")
                                focusManager.clearFocus()
                            }
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(state.jsonString))
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        }
                    )

                    if (searchVisible) {
                        SearchBar(
                            query = state.searchQuery,
                            matchCount = state.matches.size,
                            currentMatch = state.currentMatchIndex,
                            isSearching = state.isSearching,
                            onQueryChange = viewModel::updateSearchQuery,
                            onNext = viewModel::goToNextMatch,
                            onPrevious = viewModel::goToPreviousMatch
                        )
                    }

                    JsonTreeViewer(
                        rows = state.rows,
                        onToggleNode = viewModel::toggleNode,
                        listState = listState,
                        matchedPaths = state.matchedPaths,
                        focusedPath = state.focusedPath,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    BottomActions(
                        onEdit = { onNavigateToEdit(filePath, fileName) },
                        onShare = {
                            scope.launch {
                                if (!shareFile(filePath)) {
                                    snackbarHostState.showSnackbar(shareFailedMessage)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    fileName: String,
    fileSize: Long,
    onNavigateBack: () -> Unit,
    onToggleSearch: (() -> Unit)?,
    onCopy: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.action_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formatFileSize(fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (onToggleSearch != null) {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.action_search),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (onCopy != null) {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_copy),
                    contentDescription = stringResource(Res.string.action_copy),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    matchCount: Int,
    currentMatch: Int,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text(stringResource(Res.string.search_placeholder)) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(Res.string.action_clear_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        if (query.isNotBlank() && !isSearching) {
            Spacer(modifier = Modifier.width(8.dp))
            if (matchCount == 0) {
                Text(
                    text = stringResource(Res.string.search_no_results),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(Res.string.search_match_position, currentMatch + 1, matchCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(Res.string.action_previous_match),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.action_next_match),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(error: JsonDetailError, modifier: Modifier = Modifier) {
    val message = when (error) {
        is JsonDetailError.NotReadable -> stringResource(Res.string.error_unable_to_read)
        is JsonDetailError.TooLarge -> stringResource(
            Res.string.error_file_too_large,
            formatFileSize(error.limitInBytes)
        )
        is JsonDetailError.InvalidJson -> stringResource(Res.string.error_invalid_json)
    }
    val detail = (error as? JsonDetailError.InvalidJson)?.detail

    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            if (detail != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.error_invalid_json_detail, detail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BottomActions(onEdit: () -> Unit, onShare: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onEdit,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(vectorResource(Res.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.action_edit), style = MaterialTheme.typography.labelLarge)
        }

        if (onShare != null) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(vectorResource(Res.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.action_share), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
