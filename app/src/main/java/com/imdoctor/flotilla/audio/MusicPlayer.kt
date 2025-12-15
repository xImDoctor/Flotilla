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
    private var currentTrackId = "cats_cradle"  // Текущий трек

    /**
     * Получить resource ID для текущего трека
     */
    private fun getTrackResourceId(trackId: String): Int {
        return when (trackId) {
            "cats_cradle" -> R.raw.music_cats_cradle
            "hibiscus" -> R.raw.music_hibiscus
            else -> R.raw.music_cats_cradle  // Default
        }
    }

    /**
     * Запуск музыки
     */
    fun start() {
        if (!isEnabled) return

        try {
            if (mediaPlayer == null) {
                val resourceId = getTrackResourceId(currentTrackId)
                mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f) // 50% volume for background music
                }
            }
            mediaPlayer?.start()
            isPausedByUser = false
            Logger.d(TAG, "Background music started (track: $currentTrackId)")
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
        if (enabled) {
            isPausedByUser = false  // Сбрасываем флаг при включении
            start()
        } else {
            stop()  // Используем stop() вместо pause() для освобождения ресурсов
        }
    }

    /**
     * Сменить фоновый трек
     *
     * @param trackId ID трека ("cats_cradle" или "hibiscus")
     */
    fun setTrack(trackId: String) {
        if (currentTrackId == trackId) return  // Трек уже играет

        val wasPlaying = mediaPlayer?.isPlaying == true
        currentTrackId = trackId

        // Остановить и освободить текущий плеер
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Если музыка была включена, запустить новый трек
        if (wasPlaying && isEnabled) {
            start()
        }

        Logger.d(TAG, "Switched to track: $trackId")
    }

    /**
     * Освобождение ресурсов
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
