package org.ivancesari.jsonreader.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Path assigned to the document root. */
const val ROOT_PATH = "root"

/**
 * A single rendered line of the JSON tree.
 *
 * The viewer works on a flat list rather than a recursive composable so it can be rendered with a
 * `LazyColumn`: only the lines actually on screen are composed. A recursive `Column` composed every
 * expanded node at once, which made large documents drop frames and eventually trigger an ANR.
 */
sealed interface JsonTreeRow {

    val path: String
    val depth: Int

    /** Stable identity for `LazyColumn`; closing lines share a path with their opening line. */
    val id: String

    /** The opening line of an object or array: `"key": {` or a collapsed `"key": { Object (3) }`. */
    data class Branch(
        override val path: String,
        override val depth: Int,
        val key: String?,
        val isArrayIndex: Boolean,
        val isArray: Boolean,
        val childCount: Int,
        val isExpanded: Boolean,
        val isLast: Boolean
    ) : JsonTreeRow {
        override val id: String get() = path
    }

    /** The closing bracket of an expanded object or array. */
    data class Closing(
        override val path: String,
        override val depth: Int,
        val isArray: Boolean,
        val isLast: Boolean
    ) : JsonTreeRow {
        override val id: String get() = "$path/close"
    }

    /** A scalar value. */
    data class Leaf(
        override val path: String,
        override val depth: Int,
        val key: String?,
        val isArrayIndex: Boolean,
        val value: JsonPrimitive,
        val isLast: Boolean
    ) : JsonTreeRow {
        override val id: String get() = path
    }
}

/**
 * Flattens [root] into the lines that should currently be visible, given [expandedPaths].
 *
 * Iterative on purpose: a deeply nested document would overflow the stack with a recursive walk,
 * and JSON nesting depth is entirely under the control of whatever file the user opens.
 */
fun flattenJson(
    root: JsonElement,
    expandedPaths: Set<String>
): List<JsonTreeRow> {
    val rows = ArrayList<JsonTreeRow>()
    val stack = ArrayDeque<PendingRow>()
    stack.addLast(PendingRow.Node(key = null, isArrayIndex = false, element = root, path = ROOT_PATH, depth = 0, isLast = true))

    while (stack.isNotEmpty()) {
        when (val pending = stack.removeLast()) {
            is PendingRow.Close -> rows.add(
                JsonTreeRow.Closing(
                    path = pending.path,
                    depth = pending.depth,
                    isArray = pending.isArray,
                    isLast = pending.isLast
                )
            )

            is PendingRow.Node -> {
                val element = pending.element
                val childCount = when (element) {
                    is JsonObject -> element.size
                    is JsonArray -> element.size
                    else -> 0
                }

                if (element !is JsonObject && element !is JsonArray) {
                    rows.add(
                        JsonTreeRow.Leaf(
                            path = pending.path,
                            depth = pending.depth,
                            key = pending.key,
                            isArrayIndex = pending.isArrayIndex,
                            value = element as JsonPrimitive,
                            isLast = pending.isLast
                        )
                    )
                    continue
                }

                val isArray = element is JsonArray
                val isExpanded = childCount > 0 && pending.path in expandedPaths

                rows.add(
                    JsonTreeRow.Branch(
                        path = pending.path,
                        depth = pending.depth,
                        key = pending.key,
                        isArrayIndex = pending.isArrayIndex,
                        isArray = isArray,
                        childCount = childCount,
                        isExpanded = isExpanded,
                        isLast = pending.isLast
                    )
                )

                if (!isExpanded) continue

                // Pushed in reverse so children come off the stack in document order, with the
                // closing bracket emitted after all of them.
                stack.addLast(
                    PendingRow.Close(
                        path = pending.path,
                        depth = pending.depth,
                        isArray = isArray,
                        isLast = pending.isLast
                    )
                )

                when (element) {
                    is JsonObject -> {
                        val entries = element.entries.toList()
                        for (index in entries.indices.reversed()) {
                            val (key, value) = entries[index]
                            stack.addLast(
                                PendingRow.Node(
                                    key = key,
                                    isArrayIndex = false,
                                    element = value,
                                    path = "${pending.path}.$key",
                                    depth = pending.depth + 1,
                                    isLast = index == entries.lastIndex
                                )
                            )
                        }
                    }

                    is JsonArray -> {
                        for (index in element.indices.reversed()) {
                            stack.addLast(
                                PendingRow.Node(
                                    key = index.toString(),
                                    isArrayIndex = true,
                                    element = element[index],
                                    path = "${pending.path}[$index]",
                                    depth = pending.depth + 1,
                                    isLast = index == element.lastIndex
                                )
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    return rows
}

/** Result of running a query over a document. */
data class JsonSearchResult(
    /** Paths of matching rows, in document order. */
    val matches: List<String> = emptyList(),
    /** Same paths as a set, for constant-time highlight lookups. */
    val matchedPaths: Set<String> = emptySet(),
    /** Container paths that must be expanded for every match to be reachable. */
    val pathsToExpand: Set<String> = emptySet()
)

/**
 * Finds every key or scalar value containing [query], case-insensitively.
 *
 * Also collects the containers on the way to each match, so the caller can expand exactly the
 * branches needed to reveal them instead of expanding the whole document.
 */
fun searchJson(root: JsonElement, query: String): JsonSearchResult {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return JsonSearchResult()

    val needle = trimmed
    val matches = ArrayList<String>()
    val pathsToExpand = LinkedHashSet<String>()

    // Ancestors are tracked as a shared parent chain rather than a copied list per node: keys may
    // contain '.' or '[', so the path string cannot be parsed back into its parts, and copying a
    // list at every node would cost O(nodes x depth) on large documents.
    val stack = ArrayDeque<SearchFrame>()
    stack.addLast(SearchFrame(key = null, element = root, path = ROOT_PATH, parent = null))

    while (stack.isNotEmpty()) {
        val frame = stack.removeLast()
        val element = frame.element

        val keyMatches = frame.key?.contains(needle, ignoreCase = true) == true
        val valueMatches = element is JsonPrimitive && element.content.contains(needle, ignoreCase = true)

        if (keyMatches || valueMatches) {
            matches.add(frame.path)
            var ancestor = frame.parent
            while (ancestor != null && pathsToExpand.add(ancestor.path)) {
                ancestor = ancestor.parent
            }
        }

        when (element) {
            is JsonObject -> {
                val entries = element.entries.toList()
                for (index in entries.indices.reversed()) {
                    val (key, value) = entries[index]
                    stack.addLast(
                        SearchFrame(
                            key = key,
                            element = value,
                            path = "${frame.path}.$key",
                            parent = frame
                        )
                    )
                }
            }

            is JsonArray -> {
                for (index in element.indices.reversed()) {
                    stack.addLast(
                        SearchFrame(
                            key = index.toString(),
                            element = element[index],
                            path = "${frame.path}[$index]",
                            parent = frame
                        )
                    )
                }
            }

            else -> Unit
        }
    }

    // The stack walk visits siblings in order but pops depth-first, so matches come out in document
    // order already; sorting is unnecessary and would break on duplicate keys.
    return JsonSearchResult(
        matches = matches,
        matchedPaths = matches.toSet(),
        pathsToExpand = pathsToExpand
    )
}

private sealed interface PendingRow {
    data class Node(
        val key: String?,
        val isArrayIndex: Boolean,
        val element: JsonElement,
        val path: String,
        val depth: Int,
        val isLast: Boolean
    ) : PendingRow

    data class Close(
        val path: String,
        val depth: Int,
        val isArray: Boolean,
        val isLast: Boolean
    ) : PendingRow
}

/**
 * A node awaiting inspection during search.
 *
 * A plain class rather than a data class: [parent] makes the type self-referential, so generated
 * `equals`/`hashCode` would walk the whole chain on every comparison.
 */
private class SearchFrame(
    val key: String?,
    val element: JsonElement,
    val path: String,
    val parent: SearchFrame?
)
