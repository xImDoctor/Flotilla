package com.imdoctor.flotilla.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// настройки цветовой схемы (те же, что и основные, единый дизайн у нас)
private val DarkColorScheme = darkColorScheme(
    primary = FlotillaColors.Primary,
    onPrimary = FlotillaColors.OnPrimary,
    primaryContainer = FlotillaColors.PrimaryVariant,
    secondary = FlotillaColors.Secondary,
    onSecondary = FlotillaColors.OnSecondary,
    secondaryContainer = FlotillaColors.SecondaryVariant,
    background = FlotillaColors.Background,
    onBackground = FlotillaColors.OnBackground,
    surface = FlotillaColors.Surface,
    onSurface = FlotillaColors.OnSurface,
    error = FlotillaColors.Error
)


@Composable
fun FlotillaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val colorScheme = DarkColorScheme       // пока только "тёмная тема" в рамках минимализма

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // цвета и типография из color.kt, type.kt
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FlotillaTypography,
        content = content
    )
}