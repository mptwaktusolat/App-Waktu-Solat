package live.iqfareez.waktusolatmalaysia.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyPrayerTimes(
    val zone: String,
    val date: LocalDate,
    val hijriDate: String,
    val times: List<PrayerTime>,
) {
    companion object {
        val TIME_ZONE: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
    }
}

data class PrayerTime(val prayer: Prayer, val instant: Instant)

enum class Prayer { IMSAK, SUBUH, SYURUK, DHUHA, ZOHOR, ASAR, MAGHRIB, ISYAK }