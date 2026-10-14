package live.iqfareez.waktusolatmalaysia.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import live.iqfareez.waktusolatmalaysia.R

@Composable
internal fun HomeBottomBar(onActionClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().navigationBarsPadding()
                        .heightIn(min = 72.dp).padding(start = 12.dp, end = 88.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolbarAction(R.drawable.ic_la_bars_solid, R.string.menu, onActionClick)
                    ToolbarAction(R.drawable.ic_la_calendar, R.string.button_prayer_monthly_timetable, onActionClick)
                    ToolbarAction(R.drawable.ic_la_kaaba_solid, R.string.button_qibla, onActionClick)
                    ToolbarAction(R.drawable.ic_tasbih_plain, R.string.button_tasbih, onActionClick)
                }
            }
        }
        Box(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .widthIn(max = 600.dp).fillMaxWidth(),
            contentAlignment = Alignment.TopEnd,
        ) {
            FloatingActionButton(
                onClick = { onActionClick(R.string.share_timetable) },
                modifier = Modifier.padding(end = 16.dp).size(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(painterResource(R.drawable.ic_share), contentDescription = stringResource(R.string.share_timetable))
            }
        }
    }
}

@Composable
private fun ToolbarAction(@DrawableRes icon: Int, @StringRes label: Int, onClick: (Int) -> Unit) {
    IconButton(onClick = { onClick(label) }, modifier = Modifier.size(42.dp)) {
        Icon(painterResource(icon), contentDescription = stringResource(label), modifier = Modifier.size(28.dp))
    }
}
