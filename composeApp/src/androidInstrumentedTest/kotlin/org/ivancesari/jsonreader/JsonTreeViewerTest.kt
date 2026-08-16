package org.ivancesari.jsonreader

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import org.ivancesari.jsonreader.json.ROOT_PATH
import org.ivancesari.jsonreader.json.flattenJson
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.ivancesari.jsonreader.ui.components.JsonTreeViewer
import org.junit.Rule
import org.junit.Test

class JsonTreeViewerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val document = Json.parseToJsonElement(
        """
        {
            "testKey": "testValue",
            "nestedObject": { "innerKey": 123 }
        }
        """.trimIndent()
    )

    @Test
    fun expandingANodeRevealsItsChildren() {
        composeTestRule.setContent {
            var expandedPaths by remember { mutableStateOf(setOf(ROOT_PATH)) }

            JsonReaderTheme {
                JsonTreeViewer(
                    rows = flattenJson(document, expandedPaths),
                    onToggleNode = { path ->
                        expandedPaths = if (path in expandedPaths) expandedPaths - path else expandedPaths + path
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("\"testKey\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"innerKey\"", substring = true).assertDoesNotExist()

        // The whole row is the toggle target, not just the caret.
        composeTestRule.onNodeWithText("\"nestedObject\"", substring = true).performClick()

        composeTestRule.onNodeWithText("\"innerKey\"", substring = true).assertIsDisplayed()
    }
}
