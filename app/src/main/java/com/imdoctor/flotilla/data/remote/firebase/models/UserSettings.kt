package com.imdoctor.flotilla.data.remote.firebase.models

import com.google.firebase.firestore.PropertyName

/**
 * Модель настроек пользователя
 * 
 * Хранится как локально (DataStore), так и в Firestore для синхронизации
 * 
 * @property userId ID пользователя
 * @property nickname Никнейм
 * @property showCoordinates Показывать координаты на сетке
 * @property soundEnabled Звуковые эффекты включены
 * @property animationsEnabled Анимации включены
 * @property vibrationEnabled Вибрация включена
 * @property selectedShipSkin Выбранный скин кораблей
 */
data class UserSettings(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("nickname")
    @set:PropertyName("nickname")
    var nickname: String = "Player",

    // Настройки сетки
    @get:PropertyName("show_coordinates")
    @set:PropertyName("show_coordinates")
    var showCoordinates: Boolean = true,

    // Аудио и эффекты
    @get:PropertyName("sound_enabled")
    @set:PropertyName("sound_enabled")
    var soundEnabled: Boolean = true,

    @get:PropertyName("animations_enabled")
    @set:PropertyName("animations_enabled")
    var animationsEnabled: Boolean = true,

    @get:PropertyName("vibration_enabled")
    @set:PropertyName("vibration_enabled")
    var vibrationEnabled: Boolean = true,

    // Кастомизация
    @get:PropertyName("selected_ship_skin")
    @set:PropertyName("selected_ship_skin")
    var selectedShipSkin: String = "default"
) {
    companion object {
        // Firestore collection name
        const val COLLECTION_NAME = "user_settings"
        
        // Default values
        const val DEFAULT_NICKNAME = "Player"
        const val DEFAULT_SHIP_SKIN = "default"
    }
}
