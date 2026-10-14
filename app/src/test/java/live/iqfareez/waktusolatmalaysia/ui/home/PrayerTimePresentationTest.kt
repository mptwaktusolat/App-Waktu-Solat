package live.iqfareez.waktusolatmalaysia.ui.home

import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.model.PrayerTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PrayerTimePresentationTest {
    private val date = LocalDate.of(2026, 6, 29)
    private val timetable = DailyPrayerTimes(
        zone = "WLY01",
        date = date,
        hijriDate = "1448-01-13",
        times = Prayer.entries.zip(listOf("05:44", "05:54", "07:07", "07:35", "13:19", "16:45", "19:28", "20:44")) { prayer, time ->
            PrayerTime(prayer, instant(time))
        },
    )

    @Test
    fun `formats the reference date using API values`() {
        assertEquals("13 Muharram 1448", formatHijriDate("1448-01-13", listOf("Muharram")))
        assertEquals("WLY 01", formatZoneCode("WLY01"))
        assertEquals("OTHER-ZONE", formatZoneCode("OTHER-ZONE"))
    }

    @Test
    fun `invalid or already formatted dates are displayed unchanged`() {
        listOf("13 Muharram 1448", "1448-13-01", "1448-01-31", "invalid", "1448-00-01").forEach {
            assertEquals(it, formatHijriDate(it, listOf("Muharram")))
        }
        assertEquals("1448-02-01", formatHijriDate("1448-02-01", emptyList()))
    }

    @Test
    fun `highlights prayers at their exact start and until the next event`() {
        assertEquals(Prayer.SUBUH, timetable.currentPrayer(instant("05:54")))
        assertEquals(Prayer.SUBUH, timetable.currentPrayer(instant("07:06")))
        assertEquals(Prayer.ZOHOR, timetable.currentPrayer(instant("14:06")))
        assertEquals(Prayer.ASAR, timetable.currentPrayer(instant("16:45")))
        assertEquals(Prayer.MAGHRIB, timetable.currentPrayer(instant("19:28")))
        assertEquals(Prayer.ISYAK, timetable.currentPrayer(instant("20:44")))
    }

    @Test
    fun `auxiliary times do not highlight a prayer`() {
        listOf("05:43", "05:44", "07:07", "07:35", "13:18").forEach {
            assertNull(timetable.currentPrayer(instant(it)))
        }
    }

    @Test
    fun `does not highlight a stale timetable`() {
        assertNull(timetable.currentPrayer(date.plusDays(1).atTime(14, 6).atZone(DailyPrayerTimes.TIME_ZONE).toInstant()))
    }

    private fun instant(time: String) = date.atTime(LocalTime.parse(time)).atZone(DailyPrayerTimes.TIME_ZONE).toInstant()
}
