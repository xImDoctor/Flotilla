package com.imdoctor.flotilla.data.remote.firebase.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Модель профиля пользователя в Firestore
 * 
 * @property userId Уникальный ID пользователя (Firebase Auth UID)
 * @property nickname Никнейм игрока
 * @property createdAt Дата создания профиля (автоматически)
 * @property lastActive Последняя активность
 * @property gamesPlayed Общее количество игр
 * @property wins Количество побед
 * @property losses Количество поражений
 * @property totalShots Всего выстрелов
 * @property successfulShots Успешных выстрелов (попадания)
 */
data class UserProfile(
    @PropertyName("user_id")
    var userId: String = "",

    @PropertyName("nickname")
    var nickname: String = "Player",

    @PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null,

    @PropertyName("last_active")
    @ServerTimestamp
    var lastActive: Date? = null,

    // Статистика
    @PropertyName("games_played")
    var gamesPlayed: Int = 0,

    @PropertyName("wins")
    var wins: Int = 0,

    @PropertyName("losses")
    var losses: Int = 0,

    @PropertyName("total_shots")
    var totalShots: Int = 0,

    @PropertyName("successful_shots")
    var successfulShots: Int = 0
) {
    /**
     * Вычисляемое поле: процент побед
     */
    @get:Exclude
    val winRate: Double
        get() = if (gamesPlayed > 0) (wins.toDouble() / gamesPlayed * 100) else 0.0
    
    /**
     * Вычисляемое поле: точность стрельбы
     */
    @get:Exclude
    val accuracy: Double
        get() = if (totalShots > 0) (successfulShots.toDouble() / totalShots * 100) else 0.0
    
    companion object {
        // Firestore collection name
        const val COLLECTION_NAME = "users"
    }
}
