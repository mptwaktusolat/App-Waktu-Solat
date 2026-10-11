package live.iqfareez.waktusolatmalaysia.data.repository

import live.iqfareez.waktusolatmalaysia.data.model.DailyPrayerTimes
import live.iqfareez.waktusolatmalaysia.data.model.Prayer
import live.iqfareez.waktusolatmalaysia.data.model.PrayerTime
import live.iqfareez.waktusolatmalaysia.data.source.network.WaktuSolatAppApi
import java.time.Instant
import java.time.LocalDate

interface PrayerTimeRepository {
    suspend fun getPrayerTimes(zone: String, date: LocalDate): DailyPrayerTimes
}

class NetworkPrayerTimeRepository(private val api: WaktuSolatAppApi) : PrayerTimeRepository {
    override suspend fun getPrayerTimes(zone: String, date: LocalDate): DailyPrayerTimes {
        val response = api.getPrayerTimeMonthly(zone, date.year, date.monthValue)
        if (response.zone != zone || response.year != date.year || response.monthNumber != date.monthValue) {
            throw InvalidPrayerTimeDataException()
        }
        val day = response.prayers.singleOrNull { it.day == date.dayOfMonth } ?: throw InvalidPrayerTimeDataException()
        val times = listOf(
            Prayer.IMSAK to day.imsak,
            Prayer.SUBUH to day.fajr,
            Prayer.SYURUK to day.syuruk,
            Prayer.DHUHA to day.dhuha,
            Prayer.ZOHOR to day.dhuhr,
            Prayer.ASAR to day.asr,
            Prayer.MAGHRIB to day.maghrib,
            Prayer.ISYAK to day.isha,
        ).map { (prayer, seconds) ->
            val instant = Instant.ofEpochSecond(seconds)
            if (instant.atZone(DailyPrayerTimes.TIME_ZONE).toLocalDate() != date) {
                throw InvalidPrayerTimeDataException()
            }
            PrayerTime(prayer, instant)
        }
        return DailyPrayerTimes(zone, date, day.hijri, times)
    }
}

class InvalidPrayerTimeDataException : Exception("Prayer timetable is missing or invalid")
