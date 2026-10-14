package live.iqfareez.waktusolatmalaysia.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import live.iqfareez.waktusolatmalaysia.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun HomeHeader(
    date: LocalDate,
    hijriDate: String?,
    zone: String,
    onZoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val months = stringArrayResource(R.array.hijri_months)
    val zoneDescription = stringResource(R.string.select_zone, zone)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
    ) {
        Column(
            modifier = Modifier.statusBarsPadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_title),
                modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Column(
                        modifier = Modifier.heightIn(min = 72.dp).padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(date.format(DateTimeFormatter.ofPattern("EEEE", locale)), style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = hijriDate?.let { formatHijriDate(it, months.toList()) } ?: "…",
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                OutlinedButton(
                    onClick = onZoneClick,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        .semantics { contentDescription = zoneDescription },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_location_on), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatZoneCode(zone), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
