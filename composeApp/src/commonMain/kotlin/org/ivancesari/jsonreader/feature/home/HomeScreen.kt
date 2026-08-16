package org.ivancesari.jsonreader.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ivancesari.jsonreader.model.JsonFileInfo
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.new_json_file
import org.ivancesari.jsonreader.resources.no_recent_files
import org.ivancesari.jsonreader.resources.recent_files
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.ivancesari.jsonreader.ui.components.OpenFileCard
import org.ivancesari.jsonreader.ui.components.RecentFileItem
import org.ivancesari.jsonreader.ui.components.TopBar
import org.ivancesari.jsonreader.util.currentTimeMillis
import org.ivancesari.jsonreader.util.rememberFilePicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onFilePicked: (JsonFileInfo) -> Unit,
    onFileSelected: (JsonFileInfo) -> Unit,
    onRemoveRecentFile: (String) -> Unit,
    onNewFileSelected: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageShown: () -> Unit = {}
) {
    val spacing = JsonReaderTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }

    // Surfaces problems that happen before this screen is even drawn — most importantly a file
    // opened from another app that turned out to be unreadable, which used to fail silently.
    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        onMessageShown()
    }

    val openFilePicker = rememberFilePicker { pickedFile ->
        pickedFile?.let {
            val fileInfo = JsonFileInfo(
                name = it.name,
                sizeInBytes = it.sizeInBytes,
                lastOpenedTimestamp = currentTimeMillis(),
                path = it.path
            )
            onFilePicked(fileInfo)
            onFileSelected(fileInfo)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewFileSelected,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.new_json_file))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            item { TopBar() }

            item { OpenFileCard(onBrowseClick = openFilePicker) }

            item {
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(
                    text = stringResource(Res.string.recent_files),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = spacing.sm)
                )
            }

            if (uiState.recentFiles.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_recent_files),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(
                items = uiState.recentFiles,
                key = { it.path }
            ) { file ->
                RecentFileItem(
                    file = file,
                    onClick = { onFileSelected(file) },
                    onRemove = { onRemoveRecentFile(file.path) }
                )
            }
        }
    }
}
