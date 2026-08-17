package org.ivancesari.jsonreader.ui.format

import androidx.compose.runtime.Composable
import org.ivancesari.jsonreader.model.FileSizeUnit
import org.ivancesari.jsonreader.model.JsonFileInfo
import org.ivancesari.jsonreader.model.RelativeTimeUnit
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.file_size_bytes
import org.ivancesari.jsonreader.resources.file_size_gb
import org.ivancesari.jsonreader.resources.file_size_kb
import org.ivancesari.jsonreader.resources.file_size_mb
import org.ivancesari.jsonreader.resources.time_days_ago
import org.ivancesari.jsonreader.resources.time_hours_ago
import org.ivancesari.jsonreader.resources.time_just_now
import org.ivancesari.jsonreader.resources.time_last_week
import org.ivancesari.jsonreader.resources.time_minutes_ago
import org.ivancesari.jsonreader.resources.time_weeks_ago
import org.ivancesari.jsonreader.resources.time_yesterday
import org.jetbrains.compose.resources.stringResource

/**
 * Localised size and time labels.
 *
 * The formatting used to be inlined in the screens with hard-coded unit suffixes, which made those
 * strings untranslatable.
 */

@Composable
fun formatFileSize(bytes: Long): String {
    val (value, unit) = JsonFileInfo.formatSize(bytes)
    return when (unit) {
        FileSizeUnit.BYTES -> stringResource(Res.string.file_size_bytes, value)
        FileSizeUnit.KB -> stringResource(Res.string.file_size_kb, value)
        FileSizeUnit.MB -> stringResource(Res.string.file_size_mb, value)
        FileSizeUnit.GB -> stringResource(Res.string.file_size_gb, value)
    }
}

@Composable
fun formatRelativeTime(file: JsonFileInfo, now: Long): String {
    val result = file.relativeTime(now)
    return when (result.unit) {
        RelativeTimeUnit.JUST_NOW -> stringResource(Res.string.time_just_now)
        RelativeTimeUnit.MINUTES_AGO -> stringResource(Res.string.time_minutes_ago, result.value)
        RelativeTimeUnit.HOURS_AGO -> stringResource(Res.string.time_hours_ago, result.value)
        RelativeTimeUnit.YESTERDAY -> stringResource(Res.string.time_yesterday)
        RelativeTimeUnit.DAYS_AGO -> stringResource(Res.string.time_days_ago, result.value)
        RelativeTimeUnit.LAST_WEEK -> stringResource(Res.string.time_last_week)
        RelativeTimeUnit.WEEKS_AGO -> stringResource(Res.string.time_weeks_ago, result.value)
    }
}
