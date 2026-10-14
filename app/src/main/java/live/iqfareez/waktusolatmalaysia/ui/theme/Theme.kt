package live.iqfareez.waktusolatmalaysia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()
private val LocalInvertedColorScheme = staticCompositionLocalOf { DarkColorScheme }

@Composable
fun AppWaktuSolatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Use system colours on Android 12+, with Material defaults on older versions.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val lightColors = if (useDynamicColor) dynamicLightColorScheme(context) else LightColorScheme
    val darkColors = if (useDynamicColor) dynamicDarkColorScheme(context) else DarkColorScheme
    val colorScheme = if (darkTheme) darkColors else lightColors
    val invertedColorScheme = if (darkTheme) lightColors else darkColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                // The header uses the opposite scheme; the bottom bar uses the app scheme.
                isAppearanceLightStatusBars = invertedColorScheme.primaryContainer.luminance() > 0.5f
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalInvertedColorScheme provides invertedColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

/** Applies the opposite Material palette locally, without changing typography or shapes. */
@Composable
internal fun InvertedColorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LocalInvertedColorScheme.current, content = content)
}
