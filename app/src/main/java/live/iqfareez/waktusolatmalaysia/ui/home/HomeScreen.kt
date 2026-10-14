package live.iqfareez.waktusolatmalaysia.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.iqfareez.waktusolatmalaysia.R
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.model.PrayerTime
import live.iqfareez.waktusolatmalaysia.ui.theme.InvertedColorTheme
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun TodayPrayerTimesRoute(viewModel: TodayPrayerTimesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val now by produceState(Instant.now(), lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = Instant.now()
                delay(30.seconds)
            }
        }
    }
    LifecycleStartEffect(viewModel) {
        viewModel.startDateUpdates()
        onStopOrDispose { viewModel.stopDateUpdates() }
    }
    TodayPrayerTimesScreen(state = state, onRetry = viewModel::retry, currentTime = now)
}

@Composable
fun TodayPrayerTimesScreen(
    state: TodayPrayerTimesUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    currentTime: Instant = Instant.now(),
) {
    val timetable = (state as? TodayPrayerTimesUiState.Success)?.timetable
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val onPlaceholderAction: (Int) -> Unit = { label ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(resources.getString(R.string.feature_not_ready, resources.getString(label)))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            InvertedColorTheme {
                HomeHeader(
                    date = timetable?.date ?: currentTime.atZone(DailyPrayerTimes.TIME_ZONE).toLocalDate(),
                    hijriDate = timetable?.hijriDate,
                    zone = timetable?.zone ?: "WLY01",
                    onZoneClick = { onPlaceholderAction(R.string.zone_selection) },
                )
            }
        },
        bottomBar = { HomeBottomBar(onActionClick = onPlaceholderAction) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (state) {
                TodayPrayerTimesUiState.Loading -> Column(
                    Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.loading_prayer_times), style = MaterialTheme.typography.bodyMedium)
                }
                is TodayPrayerTimesUiState.Error -> Column(
                    modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth()
                        .padding(innerPadding).consumeWindowInsets(innerPadding)
                        .verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(state.reason.messageResource()), textAlign = TextAlign.Center)
                    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
                is TodayPrayerTimesUiState.Success -> PrayerTimetable(state.timetable, innerPadding, currentTime)
            }
        }
    }
}

@Composable
private fun PrayerTimetable(timetable: DailyPrayerTimes, innerPadding: PaddingValues, currentTime: Instant) {
    val layoutDirection = LocalLayoutDirection.current
    val timePattern = if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "h:mm a"
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern, Locale.ENGLISH)
        .withZone(DailyPrayerTimes.TIME_ZONE)
    val currentPrayer = timetable.currentPrayer(currentTime)

    LazyColumn(
        modifier = Modifier.widthIn(max = 408.dp).fillMaxSize().consumeWindowInsets(innerPadding),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) + 44.dp,
            end = innerPadding.calculateEndPadding(layoutDirection) + 44.dp,
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(timetable.times, key = { it.prayer }) { time ->
            PrayerTimeCard(time, timeFormatter.format(time.instant), isCurrent = time.prayer == currentPrayer)
        }
    }
}

@Composable
private fun PrayerTimeCard(time: PrayerTime, formattedTime: String, isCurrent: Boolean) {
    val isAuxiliary = time.prayer == Prayer.IMSAK || time.prayer == Prayer.SYURUK || time.prayer == Prayer.DHUHA
    val currentDescription = stringResource(R.string.current_prayer)
    Surface(
        modifier = Modifier.fillMaxWidth().semantics {
            if (isCurrent) stateDescription = currentDescription
        },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier.heightIn(min = if (isAuxiliary) 44.dp else 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.prayer_time_at, stringResource(time.prayer.labelResource()), formattedTime),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
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
