package com.imdoctor.flotilla.presentation.theme

import androidx.compose.ui.graphics.Color

// базированная палитра цветов под игру (минималистический стиль из тайлов)
object FlotillaColors {

    // основной цвет - морская волна
    val Primary = Color(0xFF1E88E5)
    val PrimaryVariant = Color(0xFF1565C0)
    val PrimaryDark = Color(0xFF0D47A1)
    val OnPrimary = Color(0xFFFFFFFF)

    // акцентный (доп) цвет - коралл
    val Secondary = Color(0xFFFF6F61)
    val SecondaryVariant = Color(0xFFE53935)
    val OnSecondary = Color(0xFFFFFFFF)

    // фон
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2C2C2C)
    val OnBackground = Color(0xFFE0E0E0)
    val OnSurface = Color(0xFFE0E0E0)
    val OnSurfaceVariant = Color(0xFF9E9E9E)
    val Outline = Color(0xFF424242)

    // специфичные цвета под игровую карту
    val WaterColor = Color(0xFF1565C0)       // вода
    val ShipColor = Color(0xFF757575)        // корабль
    val HitColor = Color(0xFFE53935)         // пробитие
    val MissColor = Color(0xFFFFFFFF)        // промах
    val SunkColor = Color(0xFF424242)        // потопление

    // сетка
    val GridLine = Color(0xFF424242)
    val GridBackground = Color(0xFF263238)


    // цвета под статусы
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val Error = Color(0xFFF44336)

}