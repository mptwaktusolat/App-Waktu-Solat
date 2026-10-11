package live.iqfareez.waktusolatmalaysia

import kotlinx.serialization.json.Json
import live.iqfareez.waktusolatmalaysia.data.repository.NetworkPrayerTimeRepository
import live.iqfareez.waktusolatmalaysia.data.repository.PrayerTimeRepository
import live.iqfareez.waktusolatmalaysia.data.source.network.WaktuSolatAppApi
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer {
    val prayerTimeRepository: PrayerTimeRepository by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl("https://api.waktusolat.app/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WaktuSolatAppApi::class.java)
        NetworkPrayerTimeRepository(api)
    }
}
