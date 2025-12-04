package com.imdoctor.flotilla.data.remote.firebase.models

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Модель резервации никнейма
 *
 * Хранится в отдельной коллекции для безопасной проверки уникальности
 * без доступа к полным профилям пользователей
 *
 * ID документа: nickname.lowercase() для case-insensitive проверки
 *
 * @property nickname Оригинальный никнейм с сохранением регистра
 * @property userId ID пользователя-владельца
 * @property createdAt Время резервации (автоматически)
 */
data class NicknameReservation(
    @get:PropertyName("nickname")
    @set:PropertyName("nickname")
    var nickname: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null
) {
    companion object {
        const val COLLECTION_NAME = "nicknames"
    }
}
