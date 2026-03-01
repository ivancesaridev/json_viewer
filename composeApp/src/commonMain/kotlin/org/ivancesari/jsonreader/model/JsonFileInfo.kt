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
    }

    fun formattedSize(): Pair<String, FileSizeUnit> {
        return when {
            sizeInBytes < BYTES_PER_KB -> Pair(sizeInBytes.toString(), FileSizeUnit.BYTES)
            sizeInBytes < BYTES_PER_MB -> {
                val kb = sizeInBytes.toDouble() / BYTES_PER_KB
                Pair(formatDecimal(kb), FileSizeUnit.KB)
            }
            sizeInBytes < BYTES_PER_GB -> {
                val mb = sizeInBytes.toDouble() / BYTES_PER_MB
                Pair(formatDecimal(mb), FileSizeUnit.MB)
            }
            else -> {
                val gb = sizeInBytes.toDouble() / BYTES_PER_GB
                Pair(formatDecimal(gb), FileSizeUnit.GB)
            }
        }
    }

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

    private fun formatDecimal(value: Double): String {
        val rounded = (kotlin.math.round(value * 10)) / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
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
