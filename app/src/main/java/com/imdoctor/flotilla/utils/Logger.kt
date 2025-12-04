package com.imdoctor.flotilla.utils

import android.util.Log
import com.imdoctor.flotilla.BuildConfig

/**
 * Мини-утилита для подробного логирования в debug-режиме
 *
 * В release-сборке все логи автоматически отключаются.
 */
object Logger {

    /**
     * Логирование ошибок (Error level)
     *
     * @param tag Тег для фильтрации в Logcat
     * @param message Сообщение
     * @param throwable Исключение (опц.)
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    /**
     * Логирование предупреждений (Warning level)
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }

    /**
     * Логирование информации (Info level)
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    /**
     * Логирование отладки (Debug level)
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
