package live.iqfareez.waktusolatmalaysia.ui.home

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.repository.InvalidPrayerTimeDataException
import live.iqfareez.waktusolatmalaysia.data.repository.PrayerTimeRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TodayPrayerTimesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = MutableClock(Instant.parse("2026-10-01T16:00:00Z"))

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loads WLY01 using Malaysian date even when clock zone is UTC`() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        assertEquals(TodayPrayerTimesUiState.Loading, viewModel.uiState.value)
        runCurrent()
        assertEquals(listOf("WLY01" to LocalDate.of(2026, 10, 2)), repository.requests)
        assertEquals(LocalDate.of(2026, 10, 2), (viewModel.uiState.value as TodayPrayerTimesUiState.Success).timetable.date)
    }

    @Test
    fun `connection failure becomes state and retry succeeds`() = runTest(dispatcher) {
        val repository = FakeRepository().apply { failure = IOException("Offline") }
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        runCurrent()
        assertEquals(TodayPrayerTimesUiState.Error(PrayerTimeError.CONNECTION), viewModel.uiState.value)
        repository.failure = null
        viewModel.retry()
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertEquals(true, viewModel.uiState.value is TodayPrayerTimesUiState.Success)
    }

    @Test
    fun `invalid data becomes data error`() = runTest(dispatcher) {
        val repository = FakeRepository().apply { failure = InvalidPrayerTimeDataException() }
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        runCurrent()
        assertEquals(TodayPrayerTimesUiState.Error(PrayerTimeError.DATA), viewModel.uiState.value)
    }

    @Test
    fun `cancellation does not become an error`() = runTest(dispatcher) {
        val viewModel = TodayPrayerTimesViewModel(
            FakeRepository().apply { failure = CancellationException() }, clock,
        )
        runCurrent()
        assertEquals(TodayPrayerTimesUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `foreground check does not reload unchanged date but refreshes after background rollover`() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        runCurrent()
        viewModel.startDateUpdates()
        runCurrent()
        assertEquals(1, repository.requests.size)
        viewModel.stopDateUpdates()
        clock.now = Instant.parse("2026-11-01T00:00:00Z")
        viewModel.startDateUpdates()
        runCurrent()
        assertEquals(LocalDate.of(2026, 11, 1), repository.requests.last().second)
        viewModel.stopDateUpdates()
    }

    @Test
    fun `visible screen refreshes at midnight across year boundary`() = runTest(dispatcher) {
        clock.now = Instant.parse("2026-12-31T15:59:59Z")
        val repository = FakeRepository()
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        viewModel.startDateUpdates()
        runCurrent()
        clock.now = Instant.parse("2026-12-31T16:00:00Z")
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(listOf(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1)), repository.requests.map { it.second })
        viewModel.stopDateUpdates()
    }

    @Test
    fun `retry cancels previous request and publishes latest result`() = runTest(dispatcher) {
        val first = CompletableDeferred<DailyPrayerTimes>()
        var calls = 0
        val repository = object : PrayerTimeRepository {
            override suspend fun getPrayerTimes(zone: String, date: LocalDate): DailyPrayerTimes {
                calls++
                return if (calls == 1) first.await() else timetable(zone, date)
            }
        }
        val viewModel = TodayPrayerTimesViewModel(repository, clock)
        runCurrent()
        viewModel.retry()
        runCurrent()
        val success = viewModel.uiState.value
        first.complete(timetable("OLD", LocalDate.of(2025, 1, 1)))
        runCurrent()
        assertEquals(2, calls)
        assertEquals(success, viewModel.uiState.value)
        assertEquals("WLY01", (success as TodayPrayerTimesUiState.Success).timetable.zone)
    }

    private class FakeRepository : PrayerTimeRepository {
        val requests = mutableListOf<Pair<String, LocalDate>>()
        var failure: Exception? = null
        override suspend fun getPrayerTimes(zone: String, date: LocalDate): DailyPrayerTimes {
            requests += zone to date
            failure?.let { throw it }
            return timetable(zone, date)
        }
    }

    private class MutableClock(var now: Instant, private val zone: ZoneId = ZoneOffset.UTC) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = Clock.fixed(now, zone)
    }

    companion object {
        private fun timetable(zone: String, date: LocalDate) = DailyPrayerTimes(zone, date, "1448-04-20", emptyList())
    }
}
