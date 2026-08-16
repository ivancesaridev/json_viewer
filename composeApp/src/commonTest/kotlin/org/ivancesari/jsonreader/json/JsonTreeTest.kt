package org.ivancesari.jsonreader.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonTreeTest {

    private fun parse(text: String) = Json.parseToJsonElement(text)

    @Test
    fun collapsedRootProducesOneRow() {
        val rows = flattenJson(parse("""{"a":1,"b":2}"""), expandedPaths = emptySet())
        assertEquals(1, rows.size)
        val branch = rows.single() as JsonTreeRow.Branch
        assertEquals(2, branch.childCount)
        assertFalse(branch.isExpanded)
    }

    @Test
    fun expandedObjectEmitsChildrenAndClosingRow() {
        val rows = flattenJson(parse("""{"a":1,"b":2}"""), expandedPaths = setOf(ROOT_PATH))

        assertEquals(
            listOf("root", "root.a", "root.b", "root"),
            rows.map { it.path }
        )
        assertTrue(rows.last() is JsonTreeRow.Closing)
    }

    @Test
    fun childrenKeepDocumentOrder() {
        val rows = flattenJson(
            parse("""{"first":1,"second":2,"third":3}"""),
            expandedPaths = setOf(ROOT_PATH)
        )
        val keys = rows.filterIsInstance<JsonTreeRow.Leaf>().map { it.key }
        assertEquals(listOf("first", "second", "third"), keys)
    }

    @Test
    fun onlyTheLastChildOmitsItsComma() {
        val rows = flattenJson(parse("""{"a":1,"b":2}"""), expandedPaths = setOf(ROOT_PATH))
        val leaves = rows.filterIsInstance<JsonTreeRow.Leaf>()
        assertFalse(leaves.first().isLast)
        assertTrue(leaves.last().isLast)
    }

    @Test
    fun arrayIndicesBecomePaths() {
        val rows = flattenJson(parse("""[10,20]"""), expandedPaths = setOf(ROOT_PATH))
        assertEquals(listOf("root", "root[0]", "root[1]", "root"), rows.map { it.path })
        assertTrue(rows.filterIsInstance<JsonTreeRow.Leaf>().all { it.isArrayIndex })
    }

    @Test
    fun rowIdsAreUniqueSoLazyListKeysDoNotCollide() {
        val rows = flattenJson(
            parse("""{"a":{"b":[1,2]},"c":{"d":3}}"""),
            expandedPaths = setOf(ROOT_PATH, "root.a", "root.a.b", "root.c")
        )
        assertEquals(rows.size, rows.map { it.id }.toSet().size)
    }

    @Test
    fun emptyContainersAreNotExpandable() {
        val rows = flattenJson(parse("""{"a":{},"b":[]}"""), expandedPaths = setOf(ROOT_PATH, "root.a", "root.b"))
        val children = rows.filterIsInstance<JsonTreeRow.Branch>().filter { it.path != ROOT_PATH }
        assertEquals(2, children.size)
        assertTrue(children.all { !it.isExpanded && it.childCount == 0 })
    }

    @Test
    fun deeplyNestedDocumentDoesNotOverflowTheStack() {
        // Nesting depth is dictated by whatever file the user opens, so the flattener walks the
        // document iteratively. A recursive walk — which is what the previous viewer did, one
        // composable per node — runs out of stack well before this depth.
        val depth = 1_000

        // Built directly rather than parsed: the parser is itself recursive, and this test is about
        // the flattener, not about how deep kotlinx.serialization can go.
        var element: JsonElement = JsonArray(emptyList())
        repeat(depth - 1) { element = JsonArray(listOf(element)) }

        val expanded = buildSet {
            var path = ROOT_PATH
            add(path)
            repeat(depth - 1) {
                path = "$path[0]"
                add(path)
            }
        }

        val rows = flattenJson(element, expanded)

        // Every level but the innermost (an empty array) contributes an opening and a closing row.
        assertEquals(depth * 2 - 1, rows.size)
    }

    @Test
    fun searchFindsKeysAndValuesCaseInsensitively() {
        val element = parse("""{"Name":"Ada","city":"NAMUR"}""")
        val result = searchJson(element, "nam")

        assertEquals(listOf("root.Name", "root.city"), result.matches)
        assertEquals(result.matches.toSet(), result.matchedPaths)
    }

    @Test
    fun searchReturnsMatchesInDocumentOrder() {
        val element = parse("""{"a":{"target":1},"b":"target","c":{"d":{"target":2}}}""")
        val result = searchJson(element, "target")

        assertEquals(listOf("root.a.target", "root.b", "root.c.d.target"), result.matches)
    }

    @Test
    fun searchCollectsEveryAncestorNeededToRevealAMatch() {
        val element = parse("""{"a":{"b":{"c":"found"}}}""")
        val result = searchJson(element, "found")

        assertEquals(listOf("root.a.b.c"), result.matches)
        assertEquals(setOf(ROOT_PATH, "root.a", "root.a.b"), result.pathsToExpand)
    }

    @Test
    fun expandingSearchResultsMakesEveryMatchVisible() {
        val element = parse("""{"a":{"b":{"c":"found"}},"z":1}""")
        val result = searchJson(element, "found")
        val rows = flattenJson(element, result.pathsToExpand)

        assertTrue(result.matches.all { match -> rows.any { it.path == match } })
    }

    @Test
    fun blankQueryMatchesNothing() {
        val result = searchJson(parse("""{"a":1}"""), "   ")
        assertTrue(result.matches.isEmpty())
        assertTrue(result.pathsToExpand.isEmpty())
    }

    @Test
    fun searchMatchesNullAndBooleanLiterals() {
        val element = parse("""{"a":null,"b":true}""")
        assertEquals(listOf("root.a"), searchJson(element, "null").matches)
        assertEquals(listOf("root.b"), searchJson(element, "true").matches)
    }
}
