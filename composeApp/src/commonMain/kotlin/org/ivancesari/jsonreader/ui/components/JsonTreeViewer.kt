package org.ivancesari.jsonreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonNull
import org.ivancesari.jsonreader.json.JsonTreeRow
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.action_collapse
import org.ivancesari.jsonreader.resources.action_expand
import org.ivancesari.jsonreader.resources.ic_arrow_down
import org.ivancesari.jsonreader.resources.ic_arrow_right
import org.ivancesari.jsonreader.resources.json_array_summary
import org.ivancesari.jsonreader.resources.json_object_summary
import org.ivancesari.jsonreader.theme.LocalSyntaxColors
import org.ivancesari.jsonreader.theme.SyntaxColorPalette
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private val INDENT_PER_DEPTH = 16.dp
private val GUTTER_WIDTH = 28.dp
private val MONOSPACE_SIZE = 14.sp

/**
 * Renders a flattened JSON document.
 *
 * [rows] arrives pre-computed from the view model, so expanding a large branch does not run a tree
 * walk on the frame that handles the tap, and the list itself can stay lazy: only the lines on
 * screen are composed.
 */
@Composable
fun JsonTreeViewer(
    rows: List<JsonTreeRow>,
    onToggleNode: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    matchedPaths: Set<String> = emptySet(),
    focusedPath: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    val syntaxColors = LocalSyntaxColors.current

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium),
        contentPadding = contentPadding
    ) {
        items(
            items = rows,
            key = { it.id }
        ) { row ->
            JsonRow(
                row = row,
                syntaxColors = syntaxColors,
                isMatch = row !is JsonTreeRow.Closing && row.path in matchedPaths,
                isFocused = row !is JsonTreeRow.Closing && row.path == focusedPath,
                onToggleNode = onToggleNode
            )
        }
    }
}

@Composable
private fun JsonRow(
    row: JsonTreeRow,
    syntaxColors: SyntaxColorPalette,
    isMatch: Boolean,
    isFocused: Boolean,
    onToggleNode: (String) -> Unit
) {
    val expandable = row is JsonTreeRow.Branch && row.childCount > 0

    val highlight = when {
        isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        isMatch -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> null
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            // The whole line toggles, not just the caret: a 28dp icon is well below the minimum
            // comfortable touch target, and the line is the thing the user is aiming at anyway.
            .then(if (expandable) Modifier.clickable { onToggleNode(row.path) } else Modifier)
            .then(if (highlight != null) Modifier.background(highlight) else Modifier)
            .padding(start = INDENT_PER_DEPTH * row.depth, top = 4.dp, bottom = 4.dp)
    ) {
        if (expandable) {
            val branch = row as JsonTreeRow.Branch
            Icon(
                imageVector = vectorResource(
                    if (branch.isExpanded) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_right
                ),
                contentDescription = stringResource(
                    if (branch.isExpanded) Res.string.action_collapse else Res.string.action_expand
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(GUTTER_WIDTH)
                    .padding(end = 4.dp, top = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(GUTTER_WIDTH))
        }

        Text(
            text = row.render(syntaxColors),
            fontFamily = FontFamily.Monospace,
            fontSize = MONOSPACE_SIZE,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun JsonTreeRow.render(colors: SyntaxColorPalette): AnnotatedString {
    val branch = this as? JsonTreeRow.Branch
    val collapsedSummary = if (branch != null && branch.childCount > 0 && !branch.isExpanded) {
        stringResource(
            if (branch.isArray) Res.string.json_array_summary else Res.string.json_object_summary,
            branch.childCount
        )
    } else {
        null
    }

    return buildAnnotatedString {
        when (val row = this@render) {
            is JsonTreeRow.Branch -> {
                appendKey(row.key, row.isArrayIndex, colors)
                val open = if (row.isArray) "[" else "{"
                val close = if (row.isArray) "]" else "}"

                when {
                    row.childCount == 0 -> {
                        punctuation(colors, "$open$close")
                        if (!row.isLast) punctuation(colors, ",")
                    }

                    collapsedSummary != null -> {
                        punctuation(colors, "$open ")
                        withStyle(SpanStyle(color = colors.summary)) { append(collapsedSummary) }
                        punctuation(colors, " $close")
                        if (!row.isLast) punctuation(colors, ",")
                    }

                    else -> punctuation(colors, open)
                }
            }

            is JsonTreeRow.Closing -> {
                val close = if (row.isArray) "]" else "}"
                punctuation(colors, if (row.isLast) close else "$close,")
            }

            is JsonTreeRow.Leaf -> {
                appendKey(row.key, row.isArrayIndex, colors)
                val value = row.value
                when {
                    value is JsonNull -> withStyle(SpanStyle(color = colors.nullValue)) { append("null") }
                    value.isString -> withStyle(SpanStyle(color = colors.string)) { append("\"${value.content}\"") }
                    value.content == "true" || value.content == "false" ->
                        withStyle(SpanStyle(color = colors.boolean)) { append(value.content) }
                    else -> withStyle(SpanStyle(color = colors.number)) { append(value.content) }
                }
                if (!row.isLast) punctuation(colors, ",")
            }
        }
    }
}

private fun AnnotatedString.Builder.appendKey(
    key: String?,
    isArrayIndex: Boolean,
    colors: SyntaxColorPalette
) {
    if (key == null) return
    withStyle(SpanStyle(color = colors.key)) {
        append(if (isArrayIndex) key else "\"$key\"")
    }
    punctuation(colors, ": ")
}

private fun AnnotatedString.Builder.punctuation(colors: SyntaxColorPalette, text: String) {
    withStyle(SpanStyle(color = colors.punctuation)) { append(text) }
}
