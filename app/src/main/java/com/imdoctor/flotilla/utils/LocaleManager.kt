package com.imdoctor.flotilla.utils

import android.content.Context
import android.content.res.Configuration
import com.imdoctor.flotilla.utils.Logger
import java.util.Locale

private const val TAG = "LocaleManager"

/**
 * Менеджер для управления локализацией приложения
 *
 * Использует классический подход через Configuration для максимальной совместимости
 */
object LocaleManager {

    /**
     * Применяет выбранный язык ко всему приложению
     *
     * @param context Context приложения
     * @param languageCode Код языка ("en", "ru")
     */
    fun applyLocale(context: Context, languageCode: String) {
        Logger.d(TAG, "Applying locale: $languageCode")

        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale.ENGLISH
            else -> Locale.ENGLISH  // Fallback
        }

        // Устанавливаем системную локаль по умолчанию
        Locale.setDefault(locale)
        Logger.d(TAG, "Locale.setDefault() applied: ${locale.language}")

        // Обновляем конфигурацию ресурсов
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        Logger.d(TAG, "Configuration updated")

        Logger.d(TAG, "Locale applied successfully: ${locale.language}")
    }

    /**
     * Применяет сохраненную локаль при старте приложения
     *
     * @param languageCode Код языка ("en", "ru")
     */
    fun applyLocaleAtStartup(languageCode: String) {
        Logger.d(TAG, "Applying locale at startup: $languageCode")

        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale.ENGLISH
            else -> Locale.ENGLISH
        }

        // Устанавливаем системную локаль по умолчанию
        Locale.setDefault(locale)

        Logger.d(TAG, "Startup locale applied: ${locale.language}")
    }
}
