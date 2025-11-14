package com.imdoctor.flotilla.data.remote.firebase.models

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
    val userId: String = "",
    
    @PropertyName("nickname")
    val nickname: String = "Player",
    
    @PropertyName("created_at")
    @ServerTimestamp
    val createdAt: Date? = null,
    
    @PropertyName("last_active")
    @ServerTimestamp
    val lastActive: Date? = null,
    
    // Статистика
    @PropertyName("games_played")
    val gamesPlayed: Int = 0,
    
    @PropertyName("wins")
    val wins: Int = 0,
    
    @PropertyName("losses")
    val losses: Int = 0,
    
    @PropertyName("total_shots")
    val totalShots: Int = 0,
    
    @PropertyName("successful_shots")
    val successfulShots: Int = 0
) {
    /**
     * Вычисляемое поле: процент побед
     */
    val winRate: Double
        get() = if (gamesPlayed > 0) (wins.toDouble() / gamesPlayed * 100) else 0.0
    
    /**
     * Вычисляемое поле: точность стрельбы
     */
    val accuracy: Double
        get() = if (totalShots > 0) (successfulShots.toDouble() / totalShots * 100) else 0.0
    
    companion object {
        // Firestore collection name
        const val COLLECTION_NAME = "users"
    }
}
