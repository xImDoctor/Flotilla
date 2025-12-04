package com.imdoctor.flotilla.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.imdoctor.flotilla.data.remote.firebase.models.GameHistory
import com.imdoctor.flotilla.data.remote.firebase.models.NicknameReservation
import com.imdoctor.flotilla.data.remote.firebase.models.UserProfile
import com.imdoctor.flotilla.data.remote.firebase.models.UserSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

/**
 * Менеджер для работы с Cloud Firestore
 * 
 * Обеспечивает:
 * - CRUD операции для пользователей, настроек и истории игр
 * - Реалтайм синхронизацию данных
 * - Безопасный доступ к данным
 */
class FirestoreManager {
    
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // ========================================
    // USER PROFILE
    // ========================================
    
    /**
     * Создание или обновление профиля пользователя
     * 
     * @param profile Профиль пользователя
     * @return Result с успехом или ошибкой
     */
    suspend fun createOrUpdateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            // Логирование данных перед отправкой (только в debug)
            com.imdoctor.flotilla.utils.Logger.d(
                "FirestoreManager",
                "Creating user profile: userId=${profile.userId}, nickname=${profile.nickname}, " +
                        "gamesPlayed=${profile.gamesPlayed}, wins=${profile.wins}, losses=${profile.losses}, " +
                        "totalShots=${profile.totalShots}, successfulShots=${profile.successfulShots}"
            )

            // Создаем Map вручную, исключая null ServerTimestamp поля
            val data = hashMapOf<String, Any>(
                "user_id" to profile.userId,
                "nickname" to profile.nickname,
                "games_played" to profile.gamesPlayed,
                "wins" to profile.wins,
                "losses" to profile.losses,
                "total_shots" to profile.totalShots,
                "successful_shots" to profile.successfulShots,
                "created_at" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "last_active" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            db.collection(UserProfile.COLLECTION_NAME)
                .document(profile.userId)
                .set(data, SetOptions.merge())
                .await()

            com.imdoctor.flotilla.utils.Logger.i("FirestoreManager", "User profile created successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            com.imdoctor.flotilla.utils.Logger.e("FirestoreManager", "Failed to create user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Получение профиля пользователя
     * 
     * @param userId ID пользователя
     * @return Result с профилем или ошибкой
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val document = db.collection(UserProfile.COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
            
            val profile = document.toObject(UserProfile::class.java)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Реалтайм отслеживание профиля пользователя
     * 
     * @param userId ID пользователя
     * @return Flow с изменениями профиля
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listenerRegistration = db.collection(UserProfile.COLLECTION_NAME)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    // ========================================
    // NICKNAME RESERVATIONS (безопасная проверка уникальности)
    // ========================================

    /**
     * Проверка доступности никнейма (безопасная версия)
     *
     * Использует отдельную коллекцию 'nicknames' вместо query по коллекции users
     * для соблюдения Security Rules
     *
     * @param nickname Никнейм для проверки
     * @param currentUserId ID текущего пользователя (чтобы исключить из проверки)
     * @return Result с true если никнейм свободен, false если занят
     */
    suspend fun checkNicknameAvailability(
        nickname: String,
        currentUserId: String
    ): Result<Boolean> {
        return try {
            val nicknameKey = nickname.lowercase(Locale.getDefault())

            val document = db.collection(NicknameReservation.COLLECTION_NAME)
                .document(nicknameKey)
                .get()
                .await()

            // Никнейм свободен если:
            // 1. Документ не существует, или
            // 2. Документ принадлежит текущему пользователю
            val isAvailable = !document.exists() ||
                document.getString("user_id") == currentUserId

            Result.success(isAvailable)
        } catch (e: Exception) {
            com.imdoctor.flotilla.utils.Logger.e("FirestoreManager", "Failed to check nickname availability", e)
            Result.failure(e)
        }
    }

    /**
     * Резервация никнейма (атомарная операция)
     *
     * Использует транзакцию для атомарной проверки и резервации
     *
     * @param nickname Никнейм для резервации
     * @param userId ID пользователя
     * @return Result с успехом или ошибкой
     */
    suspend fun reserveNickname(
        nickname: String,
        userId: String
    ): Result<Unit> {
        return try {
            val nicknameKey = nickname.lowercase(Locale.getDefault())
            val nicknameRef = db.collection(NicknameReservation.COLLECTION_NAME)
                .document(nicknameKey)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(nicknameRef)

                // Проверяем доступность
                if (snapshot.exists()) {
                    val existingUserId = snapshot.getString("user_id")
                    if (existingUserId != userId) {
                        throw Exception("Nickname already taken")
                    }
                }

                // Резервируем/обновляем
                val reservation = hashMapOf<String, Any>(
                    "nickname" to nickname,
                    "user_id" to userId,
                    "created_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                transaction.set(nicknameRef, reservation)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            com.imdoctor.flotilla.utils.Logger.e("FirestoreManager", "Failed to reserve nickname", e)
            Result.failure(e)
        }
    }

    /**
     * Освобождение старого никнейма
     *
     * Вызывается при изменении никнейма для удаления старой резервации
     *
     * @param oldNickname Старый никнейм для освобождения
     * @param userId ID пользователя (для проверки владения)
     * @return Result с успехом или ошибкой
     */
    suspend fun releaseNickname(
        oldNickname: String,
        userId: String
    ): Result<Unit> {
        return try {
            val nicknameKey = oldNickname.lowercase(Locale.getDefault())
            val nicknameRef = db.collection(NicknameReservation.COLLECTION_NAME)
                .document(nicknameKey)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(nicknameRef)

                // Удаляем только если это наш никнейм
                if (snapshot.exists() && snapshot.getString("user_id") == userId) {
                    transaction.delete(nicknameRef)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            com.imdoctor.flotilla.utils.Logger.e("FirestoreManager", "Failed to release nickname", e)
            Result.failure(e)
        }
    }

    /**
     * Обновление статистики пользователя после игры
     *
     * @param userId ID пользователя
     * @param won Победил ли пользователь
     * @param totalShots Всего выстрелов
     * @param successfulShots Успешных выстрелов
     */
    suspend fun updateUserStatistics(
        userId: String,
        won: Boolean,
        totalShots: Int,
        successfulShots: Int
    ): Result<Unit> {
        return try {
            val profileRef = db.collection(UserProfile.COLLECTION_NAME)
                .document(userId)
            
            db.runTransaction { transaction ->
                val snapshot = transaction.get(profileRef)
                val profile = snapshot.toObject(UserProfile::class.java)
                
                if (profile != null) {
                    val updatedProfile = profile.copy(
                        gamesPlayed = profile.gamesPlayed + 1,
                        wins = if (won) profile.wins + 1 else profile.wins,
                        losses = if (!won) profile.losses + 1 else profile.losses,
                        totalShots = profile.totalShots + totalShots,
                        successfulShots = profile.successfulShots + successfulShots
                    )
                    
                    transaction.set(profileRef, updatedProfile, SetOptions.merge())
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========================================
    // USER SETTINGS
    // ========================================
    
    /**
     * Сохранение настроек пользователя
     * 
     * @param settings Настройки пользователя
     */
    suspend fun saveUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            db.collection(UserSettings.COLLECTION_NAME)
                .document(settings.userId)
                .set(settings, SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение настроек пользователя
     * 
     * @param userId ID пользователя
     */
    suspend fun getUserSettings(userId: String): Result<UserSettings?> {
        return try {
            val document = db.collection(UserSettings.COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
            
            val settings = document.toObject(UserSettings::class.java)
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Реалтайм отслеживание настроек пользователя
     * 
     * @param userId ID пользователя
     */
    fun observeUserSettings(userId: String): Flow<UserSettings?> = callbackFlow {
        val listenerRegistration = db.collection(UserSettings.COLLECTION_NAME)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val settings = snapshot?.toObject(UserSettings::class.java)
                trySend(settings)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    // ========================================
    // GAME HISTORY
    // ========================================
    
    /**
     * Сохранение записи игры в историю
     * 
     * @param gameHistory Запись игры
     */
    suspend fun saveGameHistory(gameHistory: GameHistory): Result<Unit> {
        return try {
            db.collection(GameHistory.COLLECTION_NAME)
                .document(gameHistory.gameId)
                .set(gameHistory)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение истории игр пользователя
     * 
     * @param userId ID пользователя
     * @param limit Максимальное количество записей (по умолчанию 50)
     * @return Result со списком игр или ошибкой
     */
    suspend fun getUserGameHistory(
        userId: String,
        limit: Long = 50
    ): Result<List<GameHistory>> {
        return try {
            val snapshot = db.collection(GameHistory.COLLECTION_NAME)
                .whereEqualTo("user_id", userId)
                .orderBy("played_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            
            val games = snapshot.toObjects(GameHistory::class.java)
            Result.success(games)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Реалтайм отслеживание истории игр
     * 
     * @param userId ID пользователя
     * @param limit Максимальное количество записей
     */
    fun observeUserGameHistory(
        userId: String,
        limit: Long = 50
    ): Flow<List<GameHistory>> = callbackFlow {
        val listenerRegistration = db.collection(GameHistory.COLLECTION_NAME)
            .whereEqualTo("user_id", userId)
            .orderBy("played_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val games = snapshot?.toObjects(GameHistory::class.java) ?: emptyList()
                trySend(games)
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    // ========================================
    // UTILITY
    // ========================================
    
    /**
     * Удаление всех данных пользователя
     * 
     * ВНИМАНИЕ: Необратимая операция!
     * 
     * @param userId ID пользователя
     */
    suspend fun deleteUserData(userId: String): Result<Unit> {
        return try {
            db.runBatch { batch ->
                // Удаляем профиль
                batch.delete(
                    db.collection(UserProfile.COLLECTION_NAME).document(userId)
                )
                
                // Удаляем настройки
                batch.delete(
                    db.collection(UserSettings.COLLECTION_NAME).document(userId)
                )
            }.await()
            
            // Удаляем историю игр (может быть много документов)
            val historySnapshot = db.collection(GameHistory.COLLECTION_NAME)
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            
            historySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
