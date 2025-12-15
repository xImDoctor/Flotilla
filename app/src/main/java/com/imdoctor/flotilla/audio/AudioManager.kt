package com.imdoctor.flotilla.audio

import android.content.Context
import com.imdoctor.flotilla.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Центральный менеджер аудио
 *
 * Управляет фоновой музыкой и звуковыми эффектами
 * Интегрируется с настройками приложения
 */
class AudioManager(private val context: Context) {
    companion object {
        private const val TAG = "AudioManager"
    }

    private val musicPlayer = MusicPlayer(context)
    private val soundEffectPlayer = SoundEffectPlayer(context)

    /**
     * Подписка на изменения настройки музыки
     *
     * @param flow Flow с настройкой musicEnabled
     * @param scope Корутиновый scope для подписки
     */
    fun observeMusicSetting(flow: Flow<Boolean>, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            flow.collect { enabled ->
                Logger.d(TAG, "Music setting changed: $enabled")
                musicPlayer.setEnabled(enabled)
            }
        }
    }

    /**
     * Подписка на изменения выбранного фонового трека
     *
     * @param flow Flow с выбранным треком
     * @param scope Корутиновый scope для подписки
     */
    fun observeMusicTrackSetting(flow: Flow<String>, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            flow.collect { trackId ->
                Logger.d(TAG, "Music track changed: $trackId")
                musicPlayer.setTrack(trackId)
            }
        }
    }

    /**
     * Подписка на изменения настройки звуковых эффектов
     *
     * @param flow Flow с настройкой soundEffectsEnabled
     * @param scope Корутиновый scope для подписки
     */
    fun observeSoundEffectsSetting(flow: Flow<Boolean>, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            flow.collect { enabled ->
                Logger.d(TAG, "Sound effects setting changed: $enabled")
                soundEffectPlayer.setEnabled(enabled)
            }
        }
    }

    /**
     * Запуск фоновой музыки
     */
    fun startMusic() = musicPlayer.start()

    /**
     * Пауза фоновой музыки
     */
    fun pauseMusic() = musicPlayer.pause()

    /**
     * Возобновление фоновой музыки
     */
    fun resumeMusic() = musicPlayer.resume()

    /**
     * Остановка фоновой музыки
     */
    fun stopMusic() = musicPlayer.stop()

    /**
     * Воспроизведение звука попадания
     */
    fun playHit() = soundEffectPlayer.play(SoundEffectPlayer.SoundEffect.HIT)

    /**
     * Воспроизведение звука промаха
     */
    fun playMiss() = soundEffectPlayer.play(SoundEffectPlayer.SoundEffect.MISS)

    /**
     * Воспроизведение звука победы
     */
    fun playVictory() = soundEffectPlayer.play(SoundEffectPlayer.SoundEffect.VICTORY)

    /**
     * Воспроизведение звука поражения
     */
    fun playDefeat() = soundEffectPlayer.play(SoundEffectPlayer.SoundEffect.DEFEAT)

    /**
     * Обработка паузы приложения (при сворачивании)
     */
    fun onAppPause() {
        musicPlayer.pause()
        Logger.d(TAG, "App paused - music paused")
    }

    /**
     * Обработка возобновления приложения
     */
    fun onAppResume() {
        musicPlayer.resume()
        Logger.d(TAG, "App resumed - music resumed")
    }

    /**
     * Освобождение всех ресурсов
     */
    fun release() {
        musicPlayer.release()
        soundEffectPlayer.release()
        Logger.d(TAG, "AudioManager released")
    }
}
