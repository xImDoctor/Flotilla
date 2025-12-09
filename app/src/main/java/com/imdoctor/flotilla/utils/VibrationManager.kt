package com.imdoctor.flotilla.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Менеджер вибрации
 *
 * Управляет вибрацией устройства для тактильной обратной связи
 * Поддерживает Android 12+ (VibratorManager) и более старые версии
 */
class VibrationManager(private val context: Context) {
    companion object {
        private const val TAG = "VibrationManager"
        private const val HIT_VIBRATION_DURATION = 100L // 100ms для попадания
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ использует VibratorManager
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        // Старые версии Android
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isEnabled = true

    /**
     * Подписка на изменения настройки вибрации
     *
     * @param flow Flow с настройкой vibrationEnabled
     * @param scope Корутиновый scope для подписки
     */
    fun observeVibrationSetting(flow: Flow<Boolean>, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            flow.collect { enabled ->
                Logger.d(TAG, "Vibration setting changed: $enabled")
                isEnabled = enabled
            }
        }
    }

    /**
     * Вибрация при попадании по кораблю
     *
     * Короткая вибрация (100ms) для тактильной обратной связи
     */
    fun vibrateHit() {
        if (!isEnabled || vibrator?.hasVibrator() != true) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ использует VibrationEffect
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        HIT_VIBRATION_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // Старые версии Android
                @Suppress("DEPRECATION")
                vibrator?.vibrate(HIT_VIBRATION_DURATION)
            }
            Logger.d(TAG, "Vibrated on hit")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to vibrate", e)
        }
    }
}
