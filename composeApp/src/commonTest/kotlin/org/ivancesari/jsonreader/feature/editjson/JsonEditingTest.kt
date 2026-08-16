package org.ivancesari.jsonreader.feature.editjson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonEditingTest {

    /** Simulates typing [char] at the cursor, as the text field would report it. */
    private fun type(before: TextFieldValue, char: Char): TextFieldValue {
        val cursor = before.selection.start
        val text = before.text.substring(0, cursor) + char + before.text.substring(before.selection.end)
        return JsonEditing.apply(before, TextFieldValue(text, TextRange(cursor + 1)))
    }

    private fun value(text: String, cursor: Int = text.length) = TextFieldValue(text, TextRange(cursor))

    @Test
    fun typingAnOpeningBraceInsertsItsPartner() {
        val result = type(value(""), '{')
        assertEquals("{}", result.text)
        assertEquals(1, result.selection.start)
    }

    @Test
    fun typingAnOpeningBracketInsertsItsPartner() {
        val result = type(value(""), '[')
        assertEquals("[]", result.text)
        assertEquals(1, result.selection.start)
    }

    @Test
    fun typingAClosingBraceOverAnExistingOneJustMovesTheCursor() {
        val result = type(value("{}", cursor = 1), '}')
        assertEquals("{}", result.text)
        assertEquals(2, result.selection.start)
    }

    @Test
    fun enterInsideAnEmptyObjectOpensAnIndentedBlock() {
        val result = type(value("{}", cursor = 1), '\n')
        assertEquals("{\n  \n}", result.text)
        assertEquals(4, result.selection.start)
    }

    @Test
    fun enterCarriesTheCurrentIndentation() {
        val result = type(value("{\n  \"a\": 1", cursor = 10), '\n')
        assertEquals("{\n  \"a\": 1\n  ", result.text)
    }

    @Test
    fun typingAQuoteInsertsAPair() {
        val result = type(value("{\n  ", cursor = 4), '"')
        assertEquals("{\n  \"\"", result.text)
        assertEquals(5, result.selection.start)
    }

    @Test
    fun typingAQuoteOverAClosingQuoteJustMovesTheCursor() {
        val result = type(value("\"\"", cursor = 1), '"')
        assertEquals("\"\"", result.text)
        assertEquals(2, result.selection.start)
    }

    @Test
    fun deletingAnOpeningQuoteRemovesItsPartner() {
        // Cursor sits between the two quotes; backspace removes the opening one.
        val before = value("\"\"", cursor = 1)
        val result = JsonEditing.apply(before, TextFieldValue("\"", TextRange(0)))
        assertEquals("", result.text)
    }

    @Test
    fun typingAtTheStartOfAnEmptyDocumentDoesNotCrash() {
        // A grow reported with the cursor still at 0 would previously index text[-1].
        val result = JsonEditing.apply(value(""), TextFieldValue("a", TextRange(0)))
        assertEquals("a", result.text)
    }

    @Test
    fun pastingMultipleCharactersIsLeftUntouched() {
        val before = value("")
        val pasted = TextFieldValue("""{"a": 1}""", TextRange(8))
        assertEquals(pasted, JsonEditing.apply(before, pasted))
    }

    @Test
    fun ordinaryCharactersPassThrough() {
        val result = type(value("{\"a\": ", cursor = 6), '1')
        assertEquals("{\"a\": 1", result.text)
        assertEquals(7, result.selection.start)
    }

    @Test
    fun deletingAnOrdinaryCharacterIsLeftUntouched() {
        val before = value("abc")
        val after = TextFieldValue("ab", TextRange(2))
        assertEquals(after, JsonEditing.apply(before, after))
    }
}
