package live.iqfareez.waktusolatmalaysia.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.source.network.WaktuSolatAppApi
import live.iqfareez.waktusolatmalaysia.data.source.network.dto.PrayerTimeDto
import live.iqfareez.waktusolatmalaysia.data.source.network.dto.SolatV2ResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class NetworkPrayerTimeRepositoryTest {
    private val date = LocalDate.of(2026, 10, 2)

    @Test
    fun `requests date month and selects day rather than array position`() = runTest {
        val api = FakeApi(response(prayers = listOf(day(3), day(2), day(1))))
        val result = NetworkPrayerTimeRepository(api).getPrayerTimes("WLY01", date)

        assertEquals(Triple("WLY01", 2026, 10), api.request)
        assertEquals(date, result.date)
        assertEquals("1448-04-20", result.hijriDate)
        assertEquals(Prayer.entries, result.times.map { it.prayer })
        assertEquals(day(2).fajr, result.times[1].instant.epochSecond)
        assertEquals(6, result.times[1].instant.atZone(DailyPrayerTimes.TIME_ZONE).hour)
    }

    @Test
    fun `rejects missing and duplicate days`() {
        for (prayers in listOf(emptyList(), listOf(day(1)), listOf(day(2), day(2)))) {
            assertThrows(InvalidPrayerTimeDataException::class.java) {
                runTest { NetworkPrayerTimeRepository(FakeApi(response(prayers))).getPrayerTimes("WLY01", date) }
            }
        }
    }

    @Test
    fun `rejects mismatched response metadata`() {
        for (response in listOf(
            response().copy(zone = "SGR01"),
            response().copy(year = 2025),
            response().copy(monthNumber = 9),
        )) {
            assertThrows(InvalidPrayerTimeDataException::class.java) {
                runTest { NetworkPrayerTimeRepository(FakeApi(response)).getPrayerTimes("WLY01", date) }
            }
        }
    }

    @Test
    fun `rejects timestamps from a different date`() {
        assertThrows(InvalidPrayerTimeDataException::class.java) {
            runTest {
                val api = FakeApi(response(listOf(day(2).copy(fajr = day(1).fajr))))
                NetworkPrayerTimeRepository(api).getPrayerTimes("WLY01", date)
            }
        }
    }

    @Test
    fun `propagates network failures`() {
        val api = object : WaktuSolatAppApi {
            override suspend fun getPrayerTimeMonthly(zone: String, year: Int, month: Int): SolatV2ResponseDto {
                throw IOException("Offline")
            }
        }
        assertThrows(IOException::class.java) {
            runTest { NetworkPrayerTimeRepository(api).getPrayerTimes("WLY01", date) }
        }
    }

    @Test
    fun `decodes absent or null optional metadata and ignores unknown keys`() {
        val json = Json { ignoreUnknownKeys = true }
        val base = """{"zone":"WLY01","year":2026,"month":"OCT","month_number":10,"prayers":[],"extra":true"""
        for (suffix in listOf("}", ",\"last_updated\":null}")) {
            assertNull(json.decodeFromString<SolatV2ResponseDto>(base + suffix).lastUpdate)
        }
    }

    private fun response(prayers: List<PrayerTimeDto> = listOf(day(2))) = SolatV2ResponseDto(
        zone = "WLY01", year = 2026, month = "OCT", monthNumber = 10, prayers = prayers,
    )

    private fun day(day: Int): PrayerTimeDto {
        val start = date.withDayOfMonth(day).atStartOfDay(DailyPrayerTimes.TIME_ZONE)
        fun at(hour: Long) = start.plusHours(hour).toEpochSecond()
        return PrayerTimeDto(day, "1448-04-20", at(5), at(6), at(7), at(8), at(13), at(16), at(19), at(20))
    }

    private class FakeApi(private val response: SolatV2ResponseDto) : WaktuSolatAppApi {
        var request: Triple<String, Int, Int>? = null
        override suspend fun getPrayerTimeMonthly(zone: String, year: Int, month: Int): SolatV2ResponseDto {
            request = Triple(zone, year, month)
            return response
        }
    }
}
