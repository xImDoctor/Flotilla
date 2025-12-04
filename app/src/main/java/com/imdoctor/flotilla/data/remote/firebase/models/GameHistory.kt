package com.imdoctor.flotilla.data.remote.firebase.models

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Модель записи игры в истории
 * 
 * @property gameId Уникальный ID игры
 * @property userId ID пользователя
 * @property opponentId ID противника (может быть "AI" для игры с ботом)
 * @property opponentNickname Никнейм противника
 * @property gameMode Режим игры (vs_ai, online_pvp)
 * @property result Результат игры (win, loss, draw)
 * @property playedAt Дата и время игры
 * @property duration Продолжительность игры в секундах
 * @property totalShots Всего выстрелов в игре
 * @property successfulShots Успешных выстрелов
 */
data class GameHistory(
    @get:PropertyName("game_id")
    @set:PropertyName("game_id")
    var gameId: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("opponent_id")
    @set:PropertyName("opponent_id")
    var opponentId: String = "",

    @get:PropertyName("opponent_nickname")
    @set:PropertyName("opponent_nickname")
    var opponentNickname: String = "",

    @get:PropertyName("game_mode")
    @set:PropertyName("game_mode")
    var gameMode: GameMode = GameMode.VS_AI,

    @get:PropertyName("result")
    @set:PropertyName("result")
    var result: GameResult = GameResult.LOSS,

    @get:PropertyName("played_at")
    @set:PropertyName("played_at")
    @ServerTimestamp
    var playedAt: Date? = null,

    @get:PropertyName("duration")
    @set:PropertyName("duration")
    var duration: Long = 0L,  // в секундах

    @get:PropertyName("total_shots")
    @set:PropertyName("total_shots")
    var totalShots: Int = 0,

    @get:PropertyName("successful_shots")
    @set:PropertyName("successful_shots")
    var successfulShots: Int = 0
) {
    companion object {
        // Firestore collection name
        const val COLLECTION_NAME = "game_history"
    }
}

/**
 * Режимы игры
 */
enum class GameMode {
    @PropertyName("vs_ai")
    VS_AI,
    
    @PropertyName("online_pvp")
    ONLINE_PVP,
    
    @PropertyName("local_wifi")
    LOCAL_WIFI;
    
    override fun toString(): String = when (this) {
        VS_AI -> "Против ИИ"
        ONLINE_PVP -> "Онлайн PvP"
        LOCAL_WIFI -> "Локальная сеть"
    }
}

/**
 * Результаты игры
 */
enum class GameResult {
    @PropertyName("win")
    WIN,
    
    @PropertyName("loss")
    LOSS,
    
    @PropertyName("draw")
    DRAW;
    
    override fun toString(): String = when (this) {
        WIN -> "Победа"
        LOSS -> "Поражение"
        DRAW -> "Ничья"
    }
}
