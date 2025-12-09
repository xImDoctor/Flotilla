package com.imdoctor.flotilla.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.utils.Logger

/**
 * Плеер звуковых эффектов
 *
 * Использует SoundPool для быстрого воспроизведения коротких звуков
 * Управляется через AudioManager
 */
class SoundEffectPlayer(context: Context) {
    companion object {
        private const val TAG = "SoundEffectPlayer"
    }

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundEffect, Int>()
    private var isEnabled = true

    /**
     * Типы звуковых эффектов
     */
    enum class SoundEffect {
        HIT, MISS, VICTORY, DEFEAT
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        // Загружаем все звуки
        soundMap[SoundEffect.HIT] = soundPool.load(context, R.raw.sound_hit, 1)
        soundMap[SoundEffect.MISS] = soundPool.load(context, R.raw.sound_miss, 1)
        soundMap[SoundEffect.VICTORY] = soundPool.load(context, R.raw.sound_victory, 1)
        soundMap[SoundEffect.DEFEAT] = soundPool.load(context, R.raw.sound_defeat, 1)

        Logger.d(TAG, "SoundPool initialized with ${soundMap.size} sounds")
    }

    /**
     * Воспроизведение звукового эффекта
     *
     * @param effect Тип эффекта
     */
    fun play(effect: SoundEffect) {
        if (!isEnabled) return

        soundMap[effect]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            Logger.d(TAG, "Playing sound effect: $effect")
        }
    }

    /**
     * Включение/отключение звуковых эффектов
     *
     * @param enabled Включены ли эффекты
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Освобождение ресурсов
     */
    fun release() {
        soundPool.release()
        Logger.d(TAG, "SoundPool released")
    }
}
