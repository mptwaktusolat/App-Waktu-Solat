package live.iqfareez.waktusolatmalaysia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.repository.InvalidPrayerTimeDataException
import live.iqfareez.waktusolatmalaysia.data.repository.PrayerTimeRepository
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

sealed interface TodayPrayerTimesUiState {
    data object Loading : TodayPrayerTimesUiState
    data class Success(val timetable: DailyPrayerTimes) : TodayPrayerTimesUiState
    data class Error(val reason: PrayerTimeError) : TodayPrayerTimesUiState
}

enum class PrayerTimeError { CONNECTION, SERVER, DATA, UNKNOWN }

class TodayPrayerTimesViewModel(
    private val repository: PrayerTimeRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val zone: String = "WLY01",
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<TodayPrayerTimesUiState>(TodayPrayerTimesUiState.Loading)
    val uiState = mutableUiState.asStateFlow()
    private var requestedDate: LocalDate? = null
    private var loadJob: Job? = null
    private var dateJob: Job? = null

    init {
        refreshIfDateChanged()
    }

    fun retry() = load(today())

    fun startDateUpdates() {
        if (dateJob?.isActive == true) return
        dateJob = viewModelScope.launch {
            while (true) {
                refreshIfDateChanged()
                val now = clock.instant()
                val midnight = now.atZone(DailyPrayerTimes.TIME_ZONE).toLocalDate()
                    .plusDays(1).atStartOfDay(DailyPrayerTimes.TIME_ZONE).toInstant()
                // Periodic rechecks also handle manual device-clock changes.
                delay(java.time.Duration.between(now, midnight).toMillis().coerceIn(1L, 60_000L).milliseconds)
            }
        }
    }

    fun stopDateUpdates() {
        dateJob?.cancel()
        dateJob = null
    }

    private fun today() = LocalDate.now(clock.withZone(DailyPrayerTimes.TIME_ZONE))

    private fun refreshIfDateChanged() {
        val date = today()
        if (date != requestedDate) load(date)
    }

    private fun load(date: LocalDate) {
        requestedDate = date
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableUiState.value = TodayPrayerTimesUiState.Loading
            try {
                val timetable = repository.getPrayerTimes(zone, date)
                // Do not publish results from a superseded request.
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                mutableUiState.value = TodayPrayerTimesUiState.Success(timetable)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                val reason = when (exception) {
                    is IOException -> PrayerTimeError.CONNECTION
                    is HttpException -> PrayerTimeError.SERVER
                    is SerializationException, is InvalidPrayerTimeDataException,
                    is DateTimeException -> PrayerTimeError.DATA
                    else -> PrayerTimeError.UNKNOWN
                }
                mutableUiState.value = TodayPrayerTimesUiState.Error(reason)
            }
        }
    }
}
