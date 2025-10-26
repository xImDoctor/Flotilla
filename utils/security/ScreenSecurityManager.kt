package com.imdoctor.flotilla.utils.security

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

// Запрещаем скринить то, что не стоит скринить, например - расстановку кораблей

/**
 * Менеджер безопасности экрана
 * Защищает конфиденциальный контент от скриншотов и записи экрана
 */
object ScreenSecurityManager {
    /**
     * Включает FLAG_SECURE для защиты от скриншотов
     */
    fun enableScreenSecurity(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * Отключает FLAG_SECURE
     */
    fun disableScreenSecurity(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

/**
 * Compose эффект для автоматической защиты экрана
 * Для PvP матчей (расстановка)
 */
@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.let {
            ScreenSecurityManager.enableScreenSecurity(it)
        }

        onDispose {
            activity?.let {
                ScreenSecurityManager.disableScreenSecurity(it)
            }
        }
    }
}