package live.iqfareez.waktusolatmalaysia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import live.iqfareez.waktusolatmalaysia.ui.home.TodayPrayerTimesRoute
import live.iqfareez.waktusolatmalaysia.ui.home.TodayPrayerTimesViewModel
import live.iqfareez.waktusolatmalaysia.ui.theme.AppWaktuSolatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppWaktuSolatTheme {
                val prayerViewModel: TodayPrayerTimesViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            TodayPrayerTimesViewModel(
                                (application as PrayerTimeApplication).container.prayerTimeRepository,
                            )
                        }
                    },
                )
                TodayPrayerTimesRoute(prayerViewModel)
            }
        }
    }
}
