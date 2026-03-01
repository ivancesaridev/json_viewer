package org.ivancesari.jsonreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.ic_arrow_down
import org.ivancesari.jsonreader.resources.ic_arrow_right
import org.ivancesari.jsonreader.theme.LocalSyntaxColors
import org.ivancesari.jsonreader.theme.SyntaxColorPalette
import org.jetbrains.compose.resources.vectorResource

@Composable
fun JsonTreeViewer(
    jsonElement: JsonElement,
    expandedPaths: Set<String>,
    onToggleNode: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val syntaxColors = LocalSyntaxColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        JsonNode(
            key = null,
            element = jsonElement,
            path = "root",
            expandedPaths = expandedPaths,
            onToggleNode = onToggleNode,
            depth = 0,
            isLast = true,
            syntaxColors = syntaxColors
        )
    }
}

@Composable
private fun JsonNode(
    key: String?,
    element: JsonElement,
    path: String,
    expandedPaths: Set<String>,
    onToggleNode: (String) -> Unit,
    depth: Int,
    isLast: Boolean,
    syntaxColors: SyntaxColorPalette
) {
    val paddingStart = (depth * 16).dp
    val isExpanded = expandedPaths.contains(path)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = paddingStart, top = 2.dp, bottom = 2.dp)
    ) {
        // Expand/Collapse Icon for objects/arrays
        if (element is JsonObject || element is JsonArray) {
            val hasChildren = when (element) {
                is JsonObject -> element.isNotEmpty()
                is JsonArray -> element.isNotEmpty()
                else -> false
            }

            if (hasChildren) {
                Icon(
                    imageVector = vectorResource(if (isExpanded) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_right),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 4.dp, top = 4.dp)
                        .clickable { onToggleNode(path) }
                )
            } else {
                Spacer(modifier = Modifier.width(28.dp)) // Placeholder for no children
            }
        } else {
            Spacer(modifier = Modifier.width(28.dp)) // Indent primitives evenly
        }

        // Content
        Column {
            when (element) {
                is JsonObject -> {
                    val text = buildAnnotatedString {
                        if (key != null) {
                            withStyle(SpanStyle(color = syntaxColors.key)) { append("\"$key\"") }
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(": ") }
                        }
                        if (element.isEmpty()) {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("{}") }
                            if (!isLast) withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(",") }
                        } else if (!isExpanded) {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("{ ") }
                            withStyle(SpanStyle(color = syntaxColors.punctuation, background = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))) {
                                append("Object (${element.size})") 
                            }
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(" }") }
                            if (!isLast) withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(",") }
                        } else {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("{") }
                        }
                    }
                    Text(text = text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    if (isExpanded && element.isNotEmpty()) {
                        val entries = element.entries.toList()
                        entries.forEachIndexed { index, entry ->
                            JsonNode(
                                key = entry.key,
                                element = entry.value,
                                path = "$path.${entry.key}",
                                expandedPaths = expandedPaths,
                                onToggleNode = onToggleNode,
                                depth = depth + 1,
                                isLast = index == entries.lastIndex,
                                syntaxColors = syntaxColors
                            )
                        }
                        Text(
                            text = if (isLast) "}" else "},",
                            color = syntaxColors.punctuation,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 0.dp) // Align closing brace
                        )
                    }
                }
                is JsonArray -> {
                    val text = buildAnnotatedString {
                        if (key != null) {
                            withStyle(SpanStyle(color = syntaxColors.key)) { append("\"$key\"") }
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(": ") }
                        }
                        if (element.isEmpty()) {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("[]") }
                            if (!isLast) withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(",") }
                        } else if (!isExpanded) {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("[ ") }
                            withStyle(SpanStyle(color = syntaxColors.punctuation, background = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))) {
                                append("Array (${element.size})") 
                            }
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(" ]") }
                            if (!isLast) withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(",") }
                        } else {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append("[") }
                        }
                    }
                    Text(text = text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                    if (isExpanded && element.isNotEmpty()) {
                        element.forEachIndexed { index, childElement ->
                            JsonNode(
                                key = index.toString(), 
                                element = childElement,
                                path = "$path[$index]",
                                expandedPaths = expandedPaths,
                                onToggleNode = onToggleNode,
                                depth = depth + 1,
                                isLast = index == element.lastIndex,
                                syntaxColors = syntaxColors
                            )
                        }
                        Text(
                            text = if (isLast) "]" else "],",
                            color = syntaxColors.punctuation,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                }
                is JsonPrimitive -> {
                    val text = buildAnnotatedString {
                        if (key != null) {
                            withStyle(SpanStyle(color = syntaxColors.key)) { 
                                val keyStr = if (path.endsWith("]")) key else "\"$key\""
                                append(keyStr) 
                            }
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(": ") }
                        }

                        when {
                            element is JsonNull -> {
                                withStyle(SpanStyle(color = syntaxColors.nullValue)) { append("null") }
                            }
                            element.isString -> {
                                withStyle(SpanStyle(color = syntaxColors.string)) { append("\"${element.content}\"") }
                            }
                            else -> {
                                val contentStr = element.content
                                val color = when (contentStr) {
                                    "true", "false" -> syntaxColors.boolean
                                    else -> syntaxColors.number
                                }
                                withStyle(SpanStyle(color = color)) { append(contentStr) }
                            }
                        }
                        
                        if (!isLast) {
                            withStyle(SpanStyle(color = syntaxColors.punctuation)) { append(",") }
                        }
                    }
                    Text(text = text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
