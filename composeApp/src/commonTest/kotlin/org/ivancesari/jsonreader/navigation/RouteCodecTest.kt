package org.ivancesari.jsonreader.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RouteCodecTest {

    @Test
    fun roundTripsContentUri() {
        val path = "content://com.android.providers.downloads.documents/document/msf%3A1000000123"
        assertEquals(path, RouteCodec.decode(RouteCodec.encode(path)))
    }

    @Test
    fun roundTripsFilePath() {
        val path = "/storage/emulated/0/Download/my data (final)?.json"
        assertEquals(path, RouteCodec.decode(RouteCodec.encode(path)))
    }

    @Test
    fun encodingRemovesRouteSeparators() {
        val encoded = RouteCodec.encode("a/b?c#d&e=f%g")
        listOf("/", "?", "#", "&", "=", "%").forEach { separator ->
            assertFalse(encoded.contains(separator), "encoded route still contains '$separator'")
        }
    }

    @Test
    fun roundTripsInputContainingTheEscapeCharacter() {
        // The previous sentinel-based codec broke here: an input already containing the sentinel
        // decoded back to something different from what went in.
        val path = "/tmp/~s~t~q/file.json"
        assertEquals(path, RouteCodec.decode(RouteCodec.encode(path)))
    }

    @Test
    fun roundTripsInputContainingOldSentinels() {
        val path = "/tmp/::sl::/::q::/file.json"
        assertEquals(path, RouteCodec.decode(RouteCodec.encode(path)))
    }

    @Test
    fun encodingIsInjective() {
        val inputs = listOf("a/b", "a~sb", "a", "~", "~~", "", "/", "%2F")
        val encoded = inputs.map { RouteCodec.encode(it) }
        assertEquals(inputs.size, encoded.toSet().size, "two different paths encoded to the same route")
    }

    @Test
    fun handlesEmptyString() {
        assertEquals("", RouteCodec.decode(RouteCodec.encode("")))
    }

    @Test
    fun decodingLeavesUnknownEscapeSequencesAlone() {
        assertEquals("~z", RouteCodec.decode("~z"))
    }
}
