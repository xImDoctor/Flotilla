package com.imdoctor.flotilla.audio

import android.content.Context
import android.media.MediaPlayer
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.utils.Logger

/**
 * Плеер фоновой музыки
 *
 * Использует MediaPlayer для воспроизведения музыки в цикле
 * Управляется через AudioManager
 */
class MusicPlayer(private val context: Context) {
    companion object {
        private const val TAG = "MusicPlayer"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isEnabled = true
    private var isPausedByUser = false

    /**
     * Запуск музыки
     */
    fun start() {
        if (!isEnabled) return

        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.music_background).apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f) // 50% volume for background music
                }
            }
            mediaPlayer?.start()
            isPausedByUser = false
            Logger.d(TAG, "Background music started")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to start music", e)
        }
    }

    /**
     * Пауза музыки
     */
    fun pause() {
        mediaPlayer?.pause()
        isPausedByUser = true
        Logger.d(TAG, "Background music paused")
    }

    /**
     * Возобновление музыки
     */
    fun resume() {
        if (!isEnabled || isPausedByUser) return
        mediaPlayer?.start()
        Logger.d(TAG, "Background music resumed")
    }

    /**
     * Остановка музыки
     */
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Logger.d(TAG, "Background music stopped")
    }

    /**
     * Включение/отключение музыки
     *
     * @param enabled Включена ли музыка
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled && isPausedByUser.not()) {
            start()
        } else {
            pause()
        }
    }

    /**
     * Освобождение ресурсов
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
