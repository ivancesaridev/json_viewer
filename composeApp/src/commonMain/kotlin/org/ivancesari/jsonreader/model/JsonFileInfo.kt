package org.ivancesari.jsonreader.model

import kotlinx.serialization.Serializable

@Serializable
data class JsonFileInfo(
    val name: String,
    val sizeInBytes: Long,
    val lastOpenedTimestamp: Long,
    val path: String
) {
    companion object {
        private const val BYTES_PER_KB = 1024L
        private const val BYTES_PER_MB = BYTES_PER_KB * 1024
        private const val BYTES_PER_GB = BYTES_PER_MB * 1024

        private const val MILLIS_PER_MINUTE = 60_000L
        private const val MILLIS_PER_HOUR = 3_600_000L
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val MILLIS_PER_WEEK = 604_800_000L

        /**
         * Splits a byte count into a display value and its unit.
         *
         * Exposed on the companion so callers that only have a raw size — the "file too large"
         * message, for instance — can format it the same way as a file entry.
         */
        fun formatSize(bytes: Long): Pair<String, FileSizeUnit> {
            val safeBytes = bytes.coerceAtLeast(0L)
            return when {
                safeBytes < BYTES_PER_KB -> Pair(safeBytes.toString(), FileSizeUnit.BYTES)
                safeBytes < BYTES_PER_MB -> Pair(formatDecimal(safeBytes.toDouble() / BYTES_PER_KB), FileSizeUnit.KB)
                safeBytes < BYTES_PER_GB -> Pair(formatDecimal(safeBytes.toDouble() / BYTES_PER_MB), FileSizeUnit.MB)
                else -> Pair(formatDecimal(safeBytes.toDouble() / BYTES_PER_GB), FileSizeUnit.GB)
            }
        }

        private fun formatDecimal(value: Double): String {
            val rounded = (kotlin.math.round(value * 10)) / 10.0
            return if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                rounded.toString()
            }
        }
    }

    fun formattedSize(): Pair<String, FileSizeUnit> = formatSize(sizeInBytes)

    fun relativeTime(currentTimeMillis: Long): RelativeTimeResult {
        val diff = currentTimeMillis - lastOpenedTimestamp
        return when {
            diff < MILLIS_PER_MINUTE -> RelativeTimeResult(RelativeTimeUnit.JUST_NOW, 0)
            diff < MILLIS_PER_HOUR -> RelativeTimeResult(
                RelativeTimeUnit.MINUTES_AGO,
                (diff / MILLIS_PER_MINUTE).toInt()
            )
            diff < MILLIS_PER_DAY -> RelativeTimeResult(
                RelativeTimeUnit.HOURS_AGO,
                (diff / MILLIS_PER_HOUR).toInt()
            )
            diff < MILLIS_PER_DAY * 2 -> RelativeTimeResult(RelativeTimeUnit.YESTERDAY, 0)
            diff < MILLIS_PER_WEEK -> RelativeTimeResult(
                RelativeTimeUnit.DAYS_AGO,
                (diff / MILLIS_PER_DAY).toInt()
            )
            diff < MILLIS_PER_WEEK * 2 -> RelativeTimeResult(RelativeTimeUnit.LAST_WEEK, 0)
            else -> RelativeTimeResult(
                RelativeTimeUnit.WEEKS_AGO,
                (diff / MILLIS_PER_WEEK).toInt()
            )
        }
    }

}

enum class FileSizeUnit { BYTES, KB, MB, GB }

enum class RelativeTimeUnit {
    JUST_NOW, MINUTES_AGO, HOURS_AGO, YESTERDAY, DAYS_AGO, LAST_WEEK, WEEKS_AGO
}

data class RelativeTimeResult(
    val unit: RelativeTimeUnit,
    val value: Int
)
