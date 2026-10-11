package live.iqfareez.waktusolatmalaysia

import android.app.Application

class PrayerTimeApplication : Application() {
    // Keeps shared dependencies alive across Activity recreation.
    val container by lazy { AppContainer() }
}