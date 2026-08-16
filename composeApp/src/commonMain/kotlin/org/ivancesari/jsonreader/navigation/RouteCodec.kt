package org.ivancesari.jsonreader.navigation

/**
 * Escapes file paths so they can travel inside a navigation route.
 *
 * Navigation builds routes as URL-like paths, so a raw `content://` URI would be split apart by its
 * own separators. The previous approach substituted the sentinels `::sl::` and `::q::`, which is not
 * reversible: a path that already contained one of those sequences decoded back to something else.
 *
 * This codec escapes the escape character itself, which makes the mapping injective — every input
 * has exactly one encoding, and every encoding decodes back to the original input.
 */
object RouteCodec {

    private const val ESCAPE = '~'

    private val escapes = mapOf(
        ESCAPE to 't',
        '/' to 's',
        '?' to 'q',
        '#' to 'h',
        '%' to 'p',
        '&' to 'a',
        '=' to 'e'
    )

    private val unescapes = escapes.entries.associate { (char, marker) -> marker to char }

    fun encode(value: String): String = buildString(value.length) {
        for (char in value) {
            val marker = escapes[char]
            if (marker != null) {
                append(ESCAPE).append(marker)
            } else {
                append(char)
            }
        }
    }

    fun decode(value: String): String = buildString(value.length) {
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char == ESCAPE && index + 1 < value.length) {
                val original = unescapes[value[index + 1]]
                if (original != null) {
                    append(original)
                    index += 2
                    continue
                }
            }
            append(char)
            index++
        }
    }
}

fun String.encodeForRoute(): String = RouteCodec.encode(this)

fun String.decodeFromRoute(): String = RouteCodec.decode(this)
