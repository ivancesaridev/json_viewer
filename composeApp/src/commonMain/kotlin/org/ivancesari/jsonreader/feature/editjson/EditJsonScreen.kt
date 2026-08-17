package org.ivancesari.jsonreader.feature.editjson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.action_cancel
import org.ivancesari.jsonreader.resources.action_close
import org.ivancesari.jsonreader.resources.action_delete_character
import org.ivancesari.jsonreader.resources.action_indent
import org.ivancesari.jsonreader.resources.action_save
import org.ivancesari.jsonreader.resources.error_file_too_large
import org.ivancesari.jsonreader.resources.error_invalid_json_on_save
import org.ivancesari.jsonreader.resources.error_save_failed
import org.ivancesari.jsonreader.resources.error_unable_to_read
import org.ivancesari.jsonreader.resources.fix_it
import org.ivancesari.jsonreader.resources.save_anyway
import org.ivancesari.jsonreader.resources.unsaved_changes_discard
import org.ivancesari.jsonreader.resources.unsaved_changes_keep
import org.ivancesari.jsonreader.resources.unsaved_changes_message
import org.ivancesari.jsonreader.resources.unsaved_changes_title
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.ivancesari.jsonreader.ui.PlatformBackHandler
import org.ivancesari.jsonreader.ui.format.formatFileSize
import org.ivancesari.jsonreader.util.rememberFileCreator
import org.jetbrains.compose.resources.stringResource

private val EDITOR_TEXT_SIZE = 14.sp
private val EDITOR_LINE_HEIGHT = 20.sp
private val GUTTER_WIDTH = 44.dp
private val EDITOR_PADDING = 16.dp

@Composable
fun EditJsonScreen(
    filePath: String,
    fileName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: EditJsonViewModel = viewModel { EditJsonViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val editorColors = JsonReaderTheme.editor

    var confirmDiscard by remember { mutableStateOf(false) }

    val fileCreator = rememberFileCreator { pickedFile ->
        pickedFile?.let { viewModel.saveFile(newFilePath = it.path, force = true) }
    }

    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath, fileName)
    }

    LaunchedEffect(saved) {
        if (saved) onNavigateBack()
    }

    val state = uiState

    fun requestClose() {
        val success = state as? EditJsonState.Success
        if (success != null && success.isDirty) confirmDiscard = true else onNavigateBack()
    }

    PlatformBackHandler(enabled = (state as? EditJsonState.Success)?.isDirty == true) {
        confirmDiscard = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .background(editorColors.background)
    ) {
        when (state) {
            is EditJsonState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is EditJsonState.Error -> {
                val message = when (val error = state.error) {
                    is EditJsonError.NotReadable -> stringResource(Res.string.error_unable_to_read)
                    is EditJsonError.TooLarge -> stringResource(
                        Res.string.error_file_too_large,
                        formatFileSize(error.limitInBytes)
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        color = editorColors.text,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is EditJsonState.Success -> {
                EditHeader(
                    fileName = state.fileName,
                    isSaving = state.isSaving,
                    // A brand new document has nothing to compare against, but still needs saving.
                    canSave = state.isDirty || state.isNewDocument,
                    onCancel = ::requestClose,
                    onSave = {
                        if (state.isNewDocument) fileCreator(state.fileName) else viewModel.saveFile()
                    }
                )

                HorizontalDivider(color = editorColors.divider)

                if (state.saveFailed) {
                    ErrorBanner(
                        message = stringResource(Res.string.error_save_failed),
                        onDismiss = viewModel::dismissSaveError
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    JsonEditor(
                        value = state.textFieldValue,
                        onValueChange = viewModel::onValueChange
                    )
                }

                HorizontalDivider(color = editorColors.divider)

                EditBottomBar(
                    onInsert = viewModel::insertCharacter,
                    onDelete = viewModel::deleteCharacterBeforeCursor
                )

                state.invalidJsonMessage?.let { problem ->
                    AlertDialog(
                        onDismissRequest = viewModel::dismissInvalidJsonPrompt,
                        title = { Text(stringResource(Res.string.error_invalid_json_on_save, problem)) },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.dismissInvalidJsonPrompt()
                                if (state.isNewDocument) {
                                    fileCreator(state.fileName)
                                } else {
                                    viewModel.saveFile(force = true)
                                }
                            }) {
                                Text(stringResource(Res.string.save_anyway))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::dismissInvalidJsonPrompt) {
                                Text(stringResource(Res.string.fix_it))
                            }
                        }
                    )
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(Res.string.unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onNavigateBack()
                }) {
                    Text(stringResource(Res.string.unsaved_changes_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) {
                    Text(stringResource(Res.string.unsaved_changes_keep))
                }
            }
        )
    }
}

@Composable
private fun EditHeader(
    fileName: String,
    isSaving: Boolean,
    canSave: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val editorColors = JsonReaderTheme.editor

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onCancel) {
            Text(stringResource(Res.string.action_cancel), color = editorColors.text)
        }

        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = editorColors.text
        )

        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            TextButton(onClick = onSave, enabled = canSave) {
                Text(
                    text = stringResource(Res.string.action_save),
                    color = if (canSave) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        editorColors.gutterText
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.action_close), color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * The text area with its line-number gutter.
 *
 * Both columns share one scroll state, so the numbers stay aligned with the text. They used to be a
 * static column next to a text field that scrolled independently, which meant the numbers were
 * wrong as soon as the document was taller than the screen.
 */
@Composable
private fun JsonEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val editorColors = JsonReaderTheme.editor
    val scrollState = rememberScrollState()
    val lineCount = remember(value.text) { value.text.count { it == '\n' } + 1 }

    val textStyle = TextStyle(
        color = editorColors.text,
        fontSize = EDITOR_TEXT_SIZE,
        fontFamily = FontFamily.Monospace,
        lineHeight = EDITOR_LINE_HEIGHT
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Painted behind the scrolling content: a gutter that scrolled with the text would leave
        // an unpainted strip below short documents.
        Box(
            modifier = Modifier
                .width(GUTTER_WIDTH)
                .fillMaxHeight()
                .background(editorColors.gutterBackground)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(GUTTER_WIDTH)
                    .padding(vertical = EDITOR_PADDING),
                horizontalAlignment = Alignment.End
            ) {
                for (line in 1..lineCount) {
                    Text(
                        text = line.toString(),
                        style = textStyle.copy(color = editorColors.gutterText),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(EDITOR_PADDING),
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun EditBottomBar(
    onInsert: (String) -> Unit,
    onDelete: () -> Unit
) {
    val editorColors = JsonReaderTheme.editor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(editorColors.background)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("{", "}", "[", "]", ":", ",", "\"").forEach { key ->
            QuickKey(
                text = key,
                description = key,
                onClick = { onInsert(key) },
                modifier = Modifier.weight(1f)
            )
        }

        QuickKey(
            text = "⇥",
            description = stringResource(Res.string.action_indent),
            onClick = { onInsert("  ") },
            modifier = Modifier.weight(1.2f)
        )

        QuickKey(
            text = "⌫",
            description = stringResource(Res.string.action_delete_character),
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickKey(
    text: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editorColors = JsonReaderTheme.editor

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .semantics { this.contentDescription = description },
        color = editorColors.keyBackground,
        contentColor = editorColors.keyContent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 18.sp)
        }
    }
}
