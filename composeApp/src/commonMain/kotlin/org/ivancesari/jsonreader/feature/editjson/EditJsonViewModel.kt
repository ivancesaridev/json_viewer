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
import org.ivancesari.jsonreader.util.readFileContent
import org.ivancesari.jsonreader.util.saveFileContent

sealed class EditJsonState {
    object Loading : EditJsonState()
    data class Error(val message: String) : EditJsonState()
    data class Success(
        val fileName: String,
        val filePath: String,
        val textFieldValue: TextFieldValue,
        val isSaving: Boolean = false,
        val saveSuccess: Boolean? = null
    ) : EditJsonState()
}

class EditJsonViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EditJsonState>(EditJsonState.Loading)
    val uiState: StateFlow<EditJsonState> = _uiState.asStateFlow()

    fun loadFile(filePath: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (filePath.isEmpty()) {
                _uiState.value = EditJsonState.Success(
                    fileName = fileName,
                    filePath = "",
                    textFieldValue = TextFieldValue("{\n  \n}")
                )
                return@launch
            }

            try {
                _uiState.value = EditJsonState.Loading
                val content = readFileContent(filePath)
                if (content == null) {
                    _uiState.value = EditJsonState.Error("Unable to read file content")
                    return@launch
                }

                _uiState.value = EditJsonState.Success(
                    fileName = fileName,
                    filePath = filePath,
                    textFieldValue = TextFieldValue(content)
                )
            } catch (e: Exception) {
                _uiState.value = EditJsonState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onValueChange(newValue: TextFieldValue) {
        val currentState = _uiState.value
        if (currentState !is EditJsonState.Success) return

        val oldValue = currentState.textFieldValue
        val oldText = oldValue.text
        val newText = newValue.text

        // Detect if a character was just typed (text length increased by 1)
        if (newText.length == oldText.length + 1) {
            val cursorIndex = newValue.selection.start
            val typedChar = newText[cursorIndex - 1]

            when (typedChar) {
                '\n' -> {
                    // Smart Indentation on Enter
                    val textBeforeCursor = newText.substring(0, cursorIndex - 1)
                    val lastNewline = textBeforeCursor.lastIndexOf('\n')
                    val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
                    val lineBefore = textBeforeCursor.substring(lineStart)
                    
                    val currentIndent = lineBefore.takeWhile { it.isWhitespace() }
                    val lastChar = lineBefore.trim().lastOrNull()
                    
                    val extraIndent = if (lastChar == '{' || lastChar == '[') "  " else ""
                    
                    // If we are between { and }, or [ and ], add extra newline for the closing char
                    val charAfterCursor = if (cursorIndex < newText.length) newText[cursorIndex] else null
                    val isBetweenPairs = (lastChar == '{' && charAfterCursor == '}') || (lastChar == '[' && charAfterCursor == ']')
                    
                    val inserted = currentIndent + extraIndent
                    val suffix = if (isBetweenPairs) "\n$currentIndent" else ""
                    
                    val updatedText = textBeforeCursor + "\n" + inserted + suffix + newText.substring(cursorIndex)
                    val updatedSelection = TextRange(cursorIndex + inserted.length)
                    
                    _uiState.update { 
                        currentState.copy(textFieldValue = TextFieldValue(updatedText, updatedSelection))
                    }
                    return
                }
                '{', '[' -> {
                    // Auto-close braces/brackets
                    val closing = if (typedChar == '{') "}" else "]"
                    val updatedText = newText.substring(0, cursorIndex) + closing + newText.substring(cursorIndex)
                    _uiState.update {
                        currentState.copy(textFieldValue = TextFieldValue(updatedText, TextRange(cursorIndex)))
                    }
                    return
                }
                '}', ']' -> {
                    val oldCursor = oldValue.selection.start
                    val charAtOldCursor = if (oldCursor < oldText.length) oldText[oldCursor] else null
                    
                    // Skip if already present
                    if (charAtOldCursor == typedChar) {
                        _uiState.update {
                            currentState.copy(textFieldValue = TextFieldValue(oldText, TextRange(oldCursor + 1)))
                        }
                        return
                    }
                    
                    // Auto un-indent if typing closing brace on indented empty line
                    val lastNewline = oldText.lastIndexOf('\n', oldCursor - 1)
                    val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
                    val lineBefore = oldText.substring(lineStart, oldCursor)
                    if (lineBefore.isNotEmpty() && lineBefore.all { it.isWhitespace() } && lineBefore.length >= 2) {
                        val updatedText = oldText.substring(0, oldCursor - 2) + typedChar + oldText.substring(oldCursor)
                        _uiState.update {
                            currentState.copy(textFieldValue = TextFieldValue(updatedText, TextRange(oldCursor - 2 + 1)))
                        }
                        return
                    }
                }
                '"' -> {
                    val oldCursor = oldValue.selection.start
                    val charAtOldCursor = if (oldCursor < oldText.length) oldText[oldCursor] else null
                    
                    // Skip if already present
                    if (charAtOldCursor == typedChar) {
                        _uiState.update {
                            currentState.copy(textFieldValue = TextFieldValue(oldText, TextRange(oldCursor + 1)))
                        }
                        return
                    } else {
                        // Count quotes on the current line
                        val lastNewline = newText.lastIndexOf('\n', cursorIndex - 1)
                        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
                        val nextNewline = newText.indexOf('\n', cursorIndex)
                        val lineEnd = if (nextNewline == -1) newText.length else nextNewline
                        val currentLine = newText.substring(lineStart, lineEnd)
                        val quoteCount = currentLine.count { it == '"' }
                        
                        if (quoteCount % 2 != 0) {
                            // Add pair only if count is odd (spare)
                            val updatedText = newText.substring(0, cursorIndex) + "\"" + newText.substring(cursorIndex)
                            _uiState.update {
                                currentState.copy(textFieldValue = TextFieldValue(updatedText, TextRange(cursorIndex)))
                            }
                            return
                        }
                    }
                }
            }
        }

        // Detect if a character was deleted (text length decreased by 1)
        if (newText.length == oldText.length - 1) {
            val oldCursor = oldValue.selection.start
            val newCursor = newValue.selection.start
            
            // If the deleted character was a quote
            if (oldCursor > 0 && oldText[oldCursor - 1] == '"') {
                val charAfterCursor = if (newCursor < newText.length) newText[newCursor] else null
                if (charAfterCursor == '"') {
                    // Count quotes on the current line in the new text
                    val lastNewline = newText.lastIndexOf('\n', newCursor - 1)
                    val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
                    val nextNewline = newText.indexOf('\n', newCursor)
                    val lineEnd = if (nextNewline == -1) newText.length else nextNewline
                    val currentLine = newText.substring(lineStart, lineEnd)
                    val quoteCount = currentLine.count { it == '"' }
                    
                    if (quoteCount % 2 != 0) {
                        // Delete the second quote to keep balance
                        val updatedText = newText.substring(0, newCursor) + newText.substring(newCursor + 1)
                        _uiState.update {
                            currentState.copy(textFieldValue = TextFieldValue(updatedText, TextRange(newCursor)))
                        }
                        return
                    }
                }
            }
        }

        _uiState.update { currentState.copy(textFieldValue = newValue) }
    }

    fun insertCharacter(char: String) {
        val currentState = _uiState.value
        if (currentState !is EditJsonState.Success) return

        val text = currentState.textFieldValue.text
        val selection = currentState.textFieldValue.selection
        
        val newText = text.substring(0, selection.start) + char + text.substring(selection.end)
        val newSelection = TextRange(selection.start + char.length)
        
        // Trigger onValueChange logic for auto-formatting if needed, 
        // but here we just pass the new TextFieldValue directly
        // because we might want to insert exactly what's requested by the button.
        // However, if the user clicks "{", they might expect the auto-formatting too.
        
        val textFieldValue = TextFieldValue(newText, newSelection)
        onValueChange(textFieldValue)
    }

    fun saveFile(newFilePath: String? = null) {
        val currentState = _uiState.value
        if (currentState !is EditJsonState.Success) return

        val pathToSave = newFilePath ?: currentState.filePath
        if (pathToSave.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { currentState.copy(isSaving = true) }
            val success = saveFileContent(pathToSave, currentState.textFieldValue.text)
            _uiState.update { currentState.copy(isSaving = false, saveSuccess = success) }
        }
    }
}
