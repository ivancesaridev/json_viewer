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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.ic_arrow_back
import org.jetbrains.compose.resources.vectorResource

@Composable
fun EditJsonScreen(
    filePath: String,
    fileName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: EditJsonViewModel = viewModel { EditJsonViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath, fileName)
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is EditJsonState.Success && state.saveSuccess == true) {
            onNavigateBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .background(Color(0xFF0F172A)) // Dark slate background from design
    ) {
        when (val state = uiState) {
            is EditJsonState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is EditJsonState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is EditJsonState.Success -> {
                EditHeader(
                    fileName = state.fileName,
                    onCancel = onNavigateBack,
                    onSave = { viewModel.saveFile() },
                    isSaving = state.isSaving
                )

                HorizontalDivider(color = Color(0xFF1E293B))

                Box(modifier = Modifier.weight(1f)) {
                    JsonEditor(
                        value = state.textFieldValue,
                        onValueChange = viewModel::onValueChange
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                EditBottomBar(
                    onInsert = viewModel::insertCharacter,
                    onDelete = {
                        val current = state.textFieldValue
                        if (current.selection.start > 0) {
                            val newText = current.text.substring(0, current.selection.start - 1) + 
                                         current.text.substring(current.selection.end)
                            viewModel.onValueChange(current.copy(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(current.selection.start - 1)
                            ))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EditHeader(
    fileName: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", color = Color.White)
        }

        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            TextButton(onClick = onSave) {
                Text("Save", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun JsonEditor(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Line numbers
        val lineCount = value.text.count { it == '\n' } + 1
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .background(Color(0xFF0F172A))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = "$i",
                    color = Color(0xFF334155),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        VerticalDivider(color = Color(0xFF1E293B))

        // Text area
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(Color(0xFF3B82F6))
        )
    }
}

@Composable
fun VerticalDivider(color: Color) {
    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(color))
}

@Composable
fun EditBottomBar(
    onInsert: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val keys = listOf("{", "}", "[", "]", ":", ",", "\"")
        keys.forEach { key ->
            QuickKey(text = key, onClick = { onInsert(key) }, modifier = Modifier.weight(1f))
        }
        
        QuickKey(
            icon = null, // Should be an indent icon
            text = "->|",
            onClick = { onInsert("  ") },
            modifier = Modifier.weight(1.2f)
        )
        
        QuickKey(
            icon = null, // Should be a backspace icon
            text = "⌫",
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickKey(
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text != null) {
                Text(text = text, color = Color.White, fontSize = 18.sp)
            } else if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            }
        }
    }
}
