package com.notisave.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.notisave.app.viewmodel.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Indigo60,
    onPrimary = Indigo10,
    primaryContainer = Indigo30,
    onPrimaryContainer = Indigo90,
    secondary = Teal60,
    onSecondary = Teal10,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = Rose60,
    onTertiary = Rose10,
    tertiaryContainer = Rose30,
    onTertiaryContainer = Rose90,
    background = Slate5,
    onBackground = Slate90,
    surface = Slate10,
    onSurface = Slate90,
    surfaceVariant = Slate20,
    onSurfaceVariant = Slate70,
    surfaceContainerLowest = Slate5,
    surfaceContainerLow = Slate10,
    surfaceContainer = Slate15,
    surfaceContainerHigh = Slate20,
    surfaceContainerHighest = Slate25,
    outline = Slate40,
    outlineVariant = Slate30,
    error = Rose50,
    onError = Color.White,
    inverseSurface = Slate90,
    inverseOnSurface = Slate10,
    inversePrimary = Indigo40
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo50,
    onPrimary = Color.White,
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Rose90,
    onTertiaryContainer = Rose10,
    background = Slate95,
    onBackground = Slate10,
    surface = Color.White,
    onSurface = Slate10,
    surfaceVariant = Slate90,
    onSurfaceVariant = Slate40,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Slate95,
    surfaceContainer = Slate90,
    surfaceContainerHigh = Slate80,
    surfaceContainerHighest = Slate70,
    outline = Slate50,
    outlineVariant = Slate80,
    error = Rose50,
    onError = Color.White,
    inverseSurface = Slate20,
    inverseOnSurface = Slate95,
    inversePrimary = Indigo80
)

@Composable
fun NotisaveTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        // Use dynamic color (Material You) on Android 12+
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotisaveTypography,
        content = content
    )
}
