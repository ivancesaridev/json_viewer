package org.ivancesari.jsonreader.feature.editjson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * The editor's typing assistance: automatic indentation, bracket and quote pairing.
 *
 * Kept as a pure function of (previous value, proposed value) so it can be unit tested without a
 * running UI — this is the most index-heavy code in the app and the easiest place for an
 * off-by-one to slip through.
 */
object JsonEditing {

    private const val INDENT = "  "

    fun apply(old: TextFieldValue, new: TextFieldValue): TextFieldValue {
        val oldText = old.text
        val newText = new.text

        if (newText.length == oldText.length + 1) {
            val cursor = new.selection.start
            // A single-character grow with the cursor at position 0 cannot be a normal insertion;
            // bail out rather than indexing before the start of the string.
            if (cursor < 1 || cursor > newText.length) return new

            return when (val typed = newText[cursor - 1]) {
                '\n' -> applyNewline(newText, cursor)
                '{', '[' -> applyOpeningBracket(newText, cursor, typed)
                '}', ']' -> applyClosingBracket(old, newText, cursor, typed)
                '"' -> applyQuote(old, newText, cursor)
                else -> new
            }
        }

        if (newText.length == oldText.length - 1) {
            return applyDeletion(old, new)
        }

        return new
    }

    /** Carries the current indentation onto the new line, opening a block if one was just started. */
    private fun applyNewline(newText: String, cursor: Int): TextFieldValue {
        val textBeforeCursor = newText.substring(0, cursor - 1)
        val lineStart = textBeforeCursor.lastIndexOf('\n') + 1
        val lineBefore = textBeforeCursor.substring(lineStart)

        val currentIndent = lineBefore.takeWhile { it.isWhitespace() }
        val lastChar = lineBefore.trim().lastOrNull()
        val extraIndent = if (lastChar == '{' || lastChar == '[') INDENT else ""

        val charAfterCursor = newText.getOrNull(cursor)
        val betweenPair = (lastChar == '{' && charAfterCursor == '}') ||
            (lastChar == '[' && charAfterCursor == ']')

        val inserted = currentIndent + extraIndent
        val suffix = if (betweenPair) "\n$currentIndent" else ""

        val updated = textBeforeCursor + "\n" + inserted + suffix + newText.substring(cursor)
        return TextFieldValue(updated, TextRange(cursor + inserted.length))
    }

    /** Inserts the matching closing bracket and leaves the cursor between the pair. */
    private fun applyOpeningBracket(newText: String, cursor: Int, typed: Char): TextFieldValue {
        val closing = if (typed == '{') "}" else "]"
        val updated = newText.substring(0, cursor) + closing + newText.substring(cursor)
        return TextFieldValue(updated, TextRange(cursor))
    }

    /**
     * Types over an existing closing bracket instead of duplicating it, and pulls a closing bracket
     * on its own line back by one indent level.
     */
    private fun applyClosingBracket(
        old: TextFieldValue,
        newText: String,
        cursor: Int,
        typed: Char
    ): TextFieldValue {
        val oldText = old.text
        val oldCursor = old.selection.start

        if (oldText.getOrNull(oldCursor) == typed) {
            return TextFieldValue(oldText, TextRange(oldCursor + 1))
        }

        val lineStart = if (oldCursor == 0) 0 else oldText.lastIndexOf('\n', oldCursor - 1) + 1
        val lineBefore = oldText.substring(lineStart, oldCursor)

        if (lineBefore.length >= INDENT.length && lineBefore.isNotEmpty() && lineBefore.all { it.isWhitespace() }) {
            val cut = oldCursor - INDENT.length
            val updated = oldText.substring(0, cut) + typed + oldText.substring(oldCursor)
            return TextFieldValue(updated, TextRange(cut + 1))
        }

        return TextFieldValue(newText, TextRange(cursor))
    }

    /** Types over a closing quote when there is one, otherwise opens a quote pair. */
    private fun applyQuote(old: TextFieldValue, newText: String, cursor: Int): TextFieldValue {
        val oldText = old.text
        val oldCursor = old.selection.start

        if (oldText.getOrNull(oldCursor) == '"') {
            return TextFieldValue(oldText, TextRange(oldCursor + 1))
        }

        // An odd number of quotes on the line means the one just typed is unbalanced, so it is an
        // opening quote and deserves a partner.
        if (quotesOnLine(newText, cursor) % 2 != 0) {
            val updated = newText.substring(0, cursor) + "\"" + newText.substring(cursor)
            return TextFieldValue(updated, TextRange(cursor))
        }

        return TextFieldValue(newText, TextRange(cursor))
    }

    /** Removes the orphaned partner when one half of a quote pair is deleted. */
    private fun applyDeletion(old: TextFieldValue, new: TextFieldValue): TextFieldValue {
        val oldText = old.text
        val newText = new.text
        val oldCursor = old.selection.start
        val newCursor = new.selection.start

        if (oldCursor <= 0 || oldText.getOrNull(oldCursor - 1) != '"') return new
        if (newText.getOrNull(newCursor) != '"') return new
        if (quotesOnLine(newText, newCursor) % 2 == 0) return new

        val updated = newText.substring(0, newCursor) + newText.substring(newCursor + 1)
        return TextFieldValue(updated, TextRange(newCursor))
    }

    private fun quotesOnLine(text: String, cursor: Int): Int {
        val lineStart = if (cursor == 0) 0 else text.lastIndexOf('\n', cursor - 1) + 1
        val nextNewline = text.indexOf('\n', cursor)
        val lineEnd = if (nextNewline == -1) text.length else nextNewline
        return text.substring(lineStart, lineEnd).count { it == '"' }
    }
}
