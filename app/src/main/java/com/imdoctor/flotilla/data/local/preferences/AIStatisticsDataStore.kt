package com.imdoctor.flotilla.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.imdoctor.flotilla.presentation.screens.game.ai.AIDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore расширение для AI статистики
 */
private val Context.aiStatsDataStore by preferencesDataStore(name = "ai_statistics")

/**
 * Локальное хранилище статистики игр против AI
 *
 * ВАЖНО: Статистика AI игр хранится только локально и НЕ синхронизируется с Firebase.
 * Это обеспечивает оффлайн работу и не засоряет облачную статистику тренировочными играми.
 */
class AIStatisticsDataStore(private val context: Context) {

    companion object {
        // Ключи для статистики лёгкого уровня
        private val EASY_WINS_KEY = intPreferencesKey("ai_easy_wins")
        private val EASY_LOSSES_KEY = intPreferencesKey("ai_easy_losses")

        // Ключи для статистики среднего уровня
        private val MEDIUM_WINS_KEY = intPreferencesKey("ai_medium_wins")
        private val MEDIUM_LOSSES_KEY = intPreferencesKey("ai_medium_losses")

        // Ключи для статистики сложного уровня
        private val HARD_WINS_KEY = intPreferencesKey("ai_hard_wins")
        private val HARD_LOSSES_KEY = intPreferencesKey("ai_hard_losses")
    }

    /**
     * Количество побед против лёгкого AI
     */
    val easyWinsFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[EASY_WINS_KEY] ?: 0
    }

    /**
     * Количество поражений от лёгкого AI
     */
    val easyLossesFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[EASY_LOSSES_KEY] ?: 0
    }

    /**
     * Количество побед против среднего AI
     */
    val mediumWinsFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[MEDIUM_WINS_KEY] ?: 0
    }

    /**
     * Количество поражений от среднего AI
     */
    val mediumLossesFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[MEDIUM_LOSSES_KEY] ?: 0
    }

    /**
     * Количество побед против сложного AI
     */
    val hardWinsFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[HARD_WINS_KEY] ?: 0
    }

    /**
     * Количество поражений от сложного AI
     */
    val hardLossesFlow: Flow<Int> = context.aiStatsDataStore.data.map { preferences ->
        preferences[HARD_LOSSES_KEY] ?: 0
    }

    /**
     * Записать победу против AI
     *
     * @param difficulty Уровень сложности AI
     */
    suspend fun recordWin(difficulty: AIDifficulty) {
        context.aiStatsDataStore.edit { preferences ->
            val key = when (difficulty) {
                AIDifficulty.EASY -> EASY_WINS_KEY
                AIDifficulty.MEDIUM -> MEDIUM_WINS_KEY
                AIDifficulty.HARD -> HARD_WINS_KEY
            }
            val currentWins = preferences[key] ?: 0
            preferences[key] = currentWins + 1
        }
    }

    /**
     * Записать поражение от AI
     *
     * @param difficulty Уровень сложности AI
     */
    suspend fun recordLoss(difficulty: AIDifficulty) {
        context.aiStatsDataStore.edit { preferences ->
            val key = when (difficulty) {
                AIDifficulty.EASY -> EASY_LOSSES_KEY
                AIDifficulty.MEDIUM -> MEDIUM_LOSSES_KEY
                AIDifficulty.HARD -> HARD_LOSSES_KEY
            }
            val currentLosses = preferences[key] ?: 0
            preferences[key] = currentLosses + 1
        }
    }

    /**
     * Получить общую статистику для уровня сложности
     *
     * @param difficulty Уровень сложности AI
     * @return Пара (побед, поражений)
     */
    fun getStatsFlow(difficulty: AIDifficulty): Flow<Pair<Int, Int>> {
        return when (difficulty) {
            AIDifficulty.EASY -> context.aiStatsDataStore.data.map { preferences ->
                val wins = preferences[EASY_WINS_KEY] ?: 0
                val losses = preferences[EASY_LOSSES_KEY] ?: 0
                Pair(wins, losses)
            }
            AIDifficulty.MEDIUM -> context.aiStatsDataStore.data.map { preferences ->
                val wins = preferences[MEDIUM_WINS_KEY] ?: 0
                val losses = preferences[MEDIUM_LOSSES_KEY] ?: 0
                Pair(wins, losses)
            }
            AIDifficulty.HARD -> context.aiStatsDataStore.data.map { preferences ->
                val wins = preferences[HARD_WINS_KEY] ?: 0
                val losses = preferences[HARD_LOSSES_KEY] ?: 0
                Pair(wins, losses)
            }
        }
    }

    /**
     * Сбросить всю статистику AI игр
     *
     * ВНИМАНИЕ: Удаляет всю локальную статистику без возможности восстановления!
     */
    suspend fun resetAllStats() {
        context.aiStatsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
