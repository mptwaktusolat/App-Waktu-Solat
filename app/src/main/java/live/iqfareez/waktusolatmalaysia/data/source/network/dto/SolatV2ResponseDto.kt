package live.iqfareez.waktusolatmalaysia.data.source.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolatV2ResponseDto(
    val zone: String,
    val year: Int,
    val month: String,
    @SerialName("month_number")
    val monthNumber: Int,
    @SerialName("last_updated")
    val lastUpdate: String? = null,
    val prayers: List<PrayerTimeDto>,
)

@Serializable
data class PrayerTimeDto(
    val day: Int,
    val hijri: String,
    val imsak: Long,
    val fajr: Long,
    val syuruk: Long,
    val dhuha: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long,
)
