package live.iqfareez.waktusolatmalaysia.ui.home

import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import java.time.Instant

internal fun formatZoneCode(zone: String): String =
    if (zone.length == 5) "${zone.take(3)} ${zone.takeLast(2)}" else zone

// Display the API's Hijri date, not a calendar conversion which may differ from JAKIM.
internal fun formatHijriDate(rawDate: String, months: List<String>): String {
    val parts = rawDate.split('-')
    if (parts.size != 3) return rawDate
    val year = parts[0].toIntOrNull() ?: return rawDate
    val month = parts[1].toIntOrNull() ?: return rawDate
    val day = parts[2].toIntOrNull() ?: return rawDate
    if (month !in 1..12 || day !in 1..30 || year <= 0) return rawDate
    val monthName = months.getOrNull(month - 1) ?: return rawDate
    return "$day $monthName $year"
}

internal fun DailyPrayerTimes.currentPrayer(now: Instant): Prayer? {
    if (now.atZone(DailyPrayerTimes.TIME_ZONE).toLocalDate() != date) return null
    val latest = times.filter { !it.instant.isAfter(now) }.maxByOrNull { it.instant }?.prayer
    // Sunrise ends Subuh; auxiliary times do not represent an active obligatory prayer.
    return latest?.takeUnless { it == Prayer.IMSAK || it == Prayer.SYURUK || it == Prayer.DHUHA }
}
