package org.ivancesari.jsonreader.model

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonFileInfoTest {

    private fun info(size: Long = 0L, opened: Long = 0L) = JsonFileInfo(
        name = "data.json",
        sizeInBytes = size,
        lastOpenedTimestamp = opened,
        path = "content://files/data.json"
    )

    @Test
    fun sizesBelowAKilobyteStayInBytes() {
        assertEquals("512" to FileSizeUnit.BYTES, JsonFileInfo.formatSize(512))
    }

    @Test
    fun wholeUnitsDropTheDecimal() {
        assertEquals("2" to FileSizeUnit.KB, JsonFileInfo.formatSize(2048))
        assertEquals("1" to FileSizeUnit.MB, JsonFileInfo.formatSize(1024L * 1024))
    }

    @Test
    fun fractionalUnitsKeepOneDecimal() {
        assertEquals("1.5" to FileSizeUnit.KB, JsonFileInfo.formatSize(1536))
    }

    @Test
    fun gigabytesAreTheLargestUnit() {
        val (_, unit) = JsonFileInfo.formatSize(5L * 1024 * 1024 * 1024)
        assertEquals(FileSizeUnit.GB, unit)
    }

    @Test
    fun negativeSizesAreTreatedAsZero() {
        // Content providers are allowed to report an unknown size as -1.
        assertEquals("0" to FileSizeUnit.BYTES, JsonFileInfo.formatSize(-1))
    }

    @Test
    fun formattedSizeUsesTheFilesOwnSize() {
        assertEquals(JsonFileInfo.formatSize(4096), info(size = 4096).formattedSize())
    }

    @Test
    fun recentTimestampsReadAsJustNow() {
        val now = 1_000_000L
        assertEquals(RelativeTimeUnit.JUST_NOW, info(opened = now - 30_000).relativeTime(now).unit)
    }

    @Test
    fun minutesHoursAndDaysAreCountedOut() {
        val now = 10_000_000_000L

        val minutes = info(opened = now - 5 * 60_000L).relativeTime(now)
        assertEquals(RelativeTimeUnit.MINUTES_AGO, minutes.unit)
        assertEquals(5, minutes.value)

        val hours = info(opened = now - 3 * 3_600_000L).relativeTime(now)
        assertEquals(RelativeTimeUnit.HOURS_AGO, hours.unit)
        assertEquals(3, hours.value)

        val days = info(opened = now - 4 * 86_400_000L).relativeTime(now)
        assertEquals(RelativeTimeUnit.DAYS_AGO, days.unit)
        assertEquals(4, days.value)
    }

    @Test
    fun theDayBeforeReadsAsYesterday() {
        val now = 10_000_000_000L
        assertEquals(
            RelativeTimeUnit.YESTERDAY,
            info(opened = now - 30 * 3_600_000L).relativeTime(now).unit
        )
    }

    @Test
    fun olderEntriesFallBackToWeeks() {
        val now = 10_000_000_000L
        val weeks = info(opened = now - 3 * 604_800_000L).relativeTime(now)
        assertEquals(RelativeTimeUnit.WEEKS_AGO, weeks.unit)
        assertEquals(3, weeks.value)
    }
}
