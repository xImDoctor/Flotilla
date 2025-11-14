package com.imdoctor.flotilla.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.imdoctor.flotilla.data.remote.firebase.models.GameHistory
import com.imdoctor.flotilla.data.remote.firebase.models.UserProfile
import com.imdoctor.flotilla.data.remote.firebase.models.UserSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
            db.collection(UserProfile.COLLECTION_NAME)
                .document(profile.userId)
                .set(profile, SetOptions.merge())  // merge = обновит только указанные поля
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
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
