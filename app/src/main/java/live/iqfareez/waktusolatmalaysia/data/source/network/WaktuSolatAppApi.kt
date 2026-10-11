package live.iqfareez.waktusolatmalaysia.data.source.network

import live.iqfareez.waktusolatmalaysia.data.source.network.dto.SolatV2ResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WaktuSolatAppApi {
    @GET("v2/solat/{zone}")
    suspend fun getPrayerTimeMonthly(
        @Path("zone") zone: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ) : SolatV2ResponseDto
}