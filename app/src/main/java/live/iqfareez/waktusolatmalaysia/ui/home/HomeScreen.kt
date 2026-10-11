package live.iqfareez.waktusolatmalaysia.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.iqfareez.waktusolatmalaysia.R
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.model.PrayerTime
import live.iqfareez.waktusolatmalaysia.ui.theme.AppWaktuSolatTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TodayPrayerTimesRoute(viewModel: TodayPrayerTimesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleStartEffect(viewModel) {
        viewModel.startDateUpdates()
        onStopOrDispose { viewModel.stopDateUpdates() }
    }
    TodayPrayerTimesScreen(state = state, onRetry = viewModel::retry)
}

@Composable
fun TodayPrayerTimesScreen(state: TodayPrayerTimesUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (state) {
                TodayPrayerTimesUiState.Loading -> Box(
                    Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                is TodayPrayerTimesUiState.Error -> Column(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                        .padding(innerPadding).consumeWindowInsets(innerPadding)
                        .verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.today_prayer_times), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(state.reason.messageResource()))
                    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
                is TodayPrayerTimesUiState.Success -> PrayerTimetable(state.timetable, innerPadding)
            }
        }
    }
}

@Composable
private fun PrayerTimetable(timetable: DailyPrayerTimes, innerPadding: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    val locale = LocalConfiguration.current.locales[0]
    val timePattern = if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "h:mm a"
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern, locale)
        .withZone(DailyPrayerTimes.TIME_ZONE)
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)

    LazyColumn(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxSize().consumeWindowInsets(innerPadding),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) + 24.dp,
            end = innerPadding.calculateEndPadding(layoutDirection) + 24.dp,
            top = innerPadding.calculateTopPadding() + 24.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.today_prayer_times), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.prayer_zone, timetable.zone), style = MaterialTheme.typography.titleMedium)
                Text(timetable.date.format(dateFormatter))
                Text(stringResource(R.string.hijri_date, timetable.hijriDate))
            }
        }
        items(timetable.times, key = { it.prayer }) { time ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(time.prayer.labelResource()),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(timeFormatter.format(time.instant), style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
            }
        }
    }
}

private fun Prayer.labelResource(): Int = when (this) {
    Prayer.IMSAK -> R.string.imsak
    Prayer.SUBUH -> R.string.subuh
    Prayer.SYURUK -> R.string.syuruk
    Prayer.DHUHA -> R.string.dhuha
    Prayer.ZOHOR -> R.string.zohor
    Prayer.ASAR -> R.string.asar
    Prayer.MAGHRIB -> R.string.maghrib
    Prayer.ISYAK -> R.string.isyak
}

private fun PrayerTimeError.messageResource(): Int = when (this) {
    PrayerTimeError.CONNECTION -> R.string.prayer_error_connection
    PrayerTimeError.SERVER -> R.string.prayer_error_server
    PrayerTimeError.DATA -> R.string.prayer_error_data
    PrayerTimeError.UNKNOWN -> R.string.prayer_error_unknown
}

@Preview(name = "Phone", showBackground = true, widthDp = 400, heightDp = 800)
@Preview(name = "Tablet", showBackground = true, widthDp = 900, heightDp = 1000)
@Preview(name = "Large text", showBackground = true, widthDp = 400, heightDp = 800, fontScale = 1.5f)
@Composable
private fun TimetablePreview() {
    val date = LocalDate.of(2026, 10, 2)
    val timetable = DailyPrayerTimes(
        zone = "WLY01",
        date = date,
        hijriDate = "1448-04-20",
        times = Prayer.entries.mapIndexed { index, prayer ->
            PrayerTime(prayer, date.atStartOfDay(DailyPrayerTimes.TIME_ZONE).plusHours(5L + index * 2).toInstant())
        },
    )
    AppWaktuSolatTheme {
        TodayPrayerTimesScreen(TodayPrayerTimesUiState.Success(timetable), onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppWaktuSolatTheme {
        TodayPrayerTimesScreen(TodayPrayerTimesUiState.Error(PrayerTimeError.CONNECTION), onRetry = {})
    }
}