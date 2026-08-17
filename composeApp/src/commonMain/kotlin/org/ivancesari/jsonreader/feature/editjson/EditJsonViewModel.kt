package org.ivancesari.jsonreader.feature.editjson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.ivancesari.jsonreader.util.FileReadResult
import org.ivancesari.jsonreader.util.readFileContent
import org.ivancesari.jsonreader.util.saveFileContent

/** Starting content for a brand new document. */
private const val NEW_DOCUMENT_TEMPLATE = "{\n  \n}"

/** Why a file could not be opened in the editor. */
sealed interface EditJsonError {
    data object NotReadable : EditJsonError
    data class TooLarge(val limitInBytes: Long) : EditJsonError
}

sealed interface EditJsonState {

    data object Loading : EditJsonState

    data class Error(val error: EditJsonError) : EditJsonState

    data class Success(
        val fileName: String,
        val filePath: String,
        val textFieldValue: TextFieldValue,
        val originalText: String,
        val isSaving: Boolean = false,
        /** Set when the user tried to save content that is not valid JSON; holds the parser detail. */
        val invalidJsonMessage: String? = null,
        /** Set when the write itself failed, so the UI can tell the user instead of staying silent. */
        val saveFailed: Boolean = false
    ) : EditJsonState {

        val isDirty: Boolean get() = textFieldValue.text != originalText

        /** True for a document that has never been written to disk. */
        val isNewDocument: Boolean get() = filePath.isEmpty()
    }
}

class EditJsonViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EditJsonState>(EditJsonState.Loading)
    val uiState: StateFlow<EditJsonState> = _uiState.asStateFlow()

    /** Emits once after a successful write so the screen can navigate away. */
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var loadedPath: String? = null

    fun loadFile(filePath: String, fileName: String) {
        if (loadedPath == filePath && _uiState.value !is EditJsonState.Loading) return
        loadedPath = filePath

        if (filePath.isEmpty()) {
            _uiState.value = EditJsonState.Success(
                fileName = fileName,
                filePath = "",
                textFieldValue = TextFieldValue(NEW_DOCUMENT_TEMPLATE, TextRange(NEW_DOCUMENT_TEMPLATE.length - 2)),
                originalText = NEW_DOCUMENT_TEMPLATE
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = EditJsonState.Loading

            when (val result = withContext(Dispatchers.IO) { readFileContent(filePath) }) {
                is FileReadResult.NotReadable ->
                    _uiState.value = EditJsonState.Error(EditJsonError.NotReadable)

                is FileReadResult.TooLarge ->
                    _uiState.value = EditJsonState.Error(EditJsonError.TooLarge(result.limitInBytes))

                is FileReadResult.Success ->
                    _uiState.value = EditJsonState.Success(
                        fileName = fileName,
                        filePath = filePath,
                        textFieldValue = TextFieldValue(result.content),
                        originalText = result.content
                    )
            }
        }
    }

    fun onValueChange(newValue: TextFieldValue) {
        val currentState = _uiState.value as? EditJsonState.Success ?: return

        val edited = JsonEditing.apply(currentState.textFieldValue, newValue)
        _uiState.value = currentState.copy(
            textFieldValue = edited,
            // Any edit clears a stale "not valid JSON" or "save failed" banner.
            invalidJsonMessage = null,
            saveFailed = false
        )
    }

    fun insertCharacter(text: String) {
        val currentState = _uiState.value as? EditJsonState.Success ?: return

        val value = currentState.textFieldValue
        val newText = value.text.substring(0, value.selection.start) + text +
            value.text.substring(value.selection.end)
        onValueChange(TextFieldValue(newText, TextRange(value.selection.start + text.length)))
    }

    fun deleteCharacterBeforeCursor() {
        val currentState = _uiState.value as? EditJsonState.Success ?: return

        val value = currentState.textFieldValue
        if (value.selection.start == 0 && value.selection.collapsed) return

        val start = if (value.selection.collapsed) value.selection.start - 1 else value.selection.start
        val newText = value.text.substring(0, start) + value.text.substring(value.selection.end)
        onValueChange(TextFieldValue(newText, TextRange(start)))
    }

    /**
     * Writes the document, validating it first.
     *
     * Invalid JSON is reported back through [EditJsonState.Success.invalidJsonMessage] rather than
     * being written straight over the user's file; [force] is set once the user confirms.
     */
    fun saveFile(newFilePath: String? = null, force: Boolean = false) {
        val currentState = _uiState.value as? EditJsonState.Success ?: return
        if (currentState.isSaving) return

        val pathToSave = newFilePath ?: currentState.filePath
        if (pathToSave.isEmpty()) return

        val content = currentState.textFieldValue.text

        viewModelScope.launch {
            if (!force) {
                val problem = withContext(Dispatchers.Default) { validationMessage(content) }
                if (problem != null) {
                    _uiState.update { state ->
                        (state as? EditJsonState.Success)?.copy(invalidJsonMessage = problem) ?: state
                    }
                    return@launch
                }
            }

            _uiState.update { state ->
                (state as? EditJsonState.Success)?.copy(
                    isSaving = true,
                    invalidJsonMessage = null,
                    saveFailed = false
                ) ?: state
            }

            val success = withContext(Dispatchers.IO) { saveFileContent(pathToSave, content) }

            // Re-read the state instead of reusing the snapshot captured before the write: the user
            // may have kept typing while the save was in flight, and reusing the old copy would
            // silently roll their edits back.
            _uiState.update { state ->
                (state as? EditJsonState.Success)?.copy(
                    isSaving = false,
                    saveFailed = !success,
                    filePath = if (success) pathToSave else state.filePath,
                    originalText = if (success) content else state.originalText
                ) ?: state
            }

            if (success) _saved.value = true
        }
    }

    fun dismissInvalidJsonPrompt() {
        _uiState.update { state ->
            (state as? EditJsonState.Success)?.copy(invalidJsonMessage = null) ?: state
        }
    }

    fun dismissSaveError() {
        _uiState.update { state ->
            (state as? EditJsonState.Success)?.copy(saveFailed = false) ?: state
        }
    }

    private fun validationMessage(content: String): String? = try {
        Json.parseToJsonElement(content)
        null
    } catch (e: Exception) {
        e.message?.takeIf { it.isNotBlank() }?.lineSequence()?.first() ?: "Unexpected character"
    }
}
