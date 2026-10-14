package live.iqfareez.waktusolatmalaysia.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.model.PrayerTime
import live.iqfareez.waktusolatmalaysia.ui.theme.AppWaktuSolatTheme
import java.time.LocalDate
import java.time.LocalTime

@Preview(name = "Reference · Malay", showBackground = true, widthDp = 390, heightDp = 844, locale = "ms")
@Preview(name = "Compact phone", showBackground = true, widthDp = 320, heightDp = 640, locale = "ms")
@Preview(name = "Tablet", showBackground = true, widthDp = 900, heightDp = 1000)
@Preview(name = "Large text", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1.5f, locale = "ms")
@Composable
private fun TimetablePreview() {
    ReferenceTimetable(darkTheme = false)
}

@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, locale = "ms")
@Composable
private fun DarkTimetablePreview() {
    ReferenceTimetable(darkTheme = true)
}

@Composable
private fun ReferenceTimetable(darkTheme: Boolean) {
    val date = LocalDate.of(2026, 6, 29)
    val times = listOf("05:44", "05:54", "07:07", "07:35", "13:19", "16:45", "19:28", "20:44")
    val timetable = DailyPrayerTimes(
        zone = "WLY01",
        date = date,
        hijriDate = "1448-01-13",
        times = Prayer.entries.zip(times) { prayer, time ->
            PrayerTime(prayer, date.atTime(LocalTime.parse(time)).atZone(DailyPrayerTimes.TIME_ZONE).toInstant())
        },
    )
    AppWaktuSolatTheme(darkTheme = darkTheme) {
        TodayPrayerTimesScreen(
            state = TodayPrayerTimesUiState.Success(timetable),
            onRetry = {},
            currentTime = date.atTime(14, 6).atZone(DailyPrayerTimes.TIME_ZONE).toInstant(),
        )
    }
}

@Preview(name = "Connection error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ErrorPreview() {
    AppWaktuSolatTheme(darkTheme = false) {
        TodayPrayerTimesScreen(TodayPrayerTimesUiState.Error(PrayerTimeError.CONNECTION), onRetry = {})
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoadingPreview() {
    AppWaktuSolatTheme(darkTheme = false) {
        TodayPrayerTimesScreen(TodayPrayerTimesUiState.Loading, onRetry = {})
    }
}
