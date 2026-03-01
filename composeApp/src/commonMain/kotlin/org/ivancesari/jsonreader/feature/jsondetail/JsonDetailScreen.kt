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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.ic_arrow_back
import org.ivancesari.jsonreader.resources.ic_copy
import org.ivancesari.jsonreader.resources.ic_edit
import org.ivancesari.jsonreader.resources.ic_search
import org.ivancesari.jsonreader.resources.ic_share
import org.ivancesari.jsonreader.ui.components.JsonTreeViewer
import org.ivancesari.jsonreader.util.shareFile
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
    val clipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is JsonDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is JsonDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is JsonDetailState.Success -> {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
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
                        // Use actual file size
                        val tempFileInfo = org.ivancesari.jsonreader.model.JsonFileInfo(
                            name = fileName,
                            sizeInBytes = fileSize,
                            lastOpenedTimestamp = 0,
                            path = filePath
                        )
                        val (sizeVal, sizeUnit) = tempFileInfo.formattedSize()
                        val sizeStr = when (sizeUnit) {
                            org.ivancesari.jsonreader.model.FileSizeUnit.BYTES -> "$sizeVal B"
                            org.ivancesari.jsonreader.model.FileSizeUnit.KB -> "$sizeVal KB"
                            org.ivancesari.jsonreader.model.FileSizeUnit.MB -> "$sizeVal MB"
                            org.ivancesari.jsonreader.model.FileSizeUnit.GB -> "$sizeVal GB"
                        }
                        Text(
                            text = sizeStr, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = { 
                        clipboardManager.setText(AnnotatedString(state.jsonString)) 
                        // could show a snackbar here
                    }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_copy),
                            contentDescription = "Copy JSON",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Json Viewer with Scroll
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            JsonTreeViewer(
                                jsonElement = state.jsonTree,
                                expandedPaths = state.expandedPaths,
                                onToggleNode = { path -> viewModel.toggleNode(path) },
                                searchQuery = state.searchQuery
                            )
                        }
                    }
                }
                
                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onNavigateToEdit(filePath, fileName) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(vectorResource(Res.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit JSON", style = MaterialTheme.typography.labelLarge)
                    }
                    
                    Button(
                        onClick = { shareFile(filePath) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(vectorResource(Res.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
