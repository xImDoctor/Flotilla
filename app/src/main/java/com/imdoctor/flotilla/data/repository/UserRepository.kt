package com.imdoctor.flotilla.data.repository

import com.imdoctor.flotilla.data.remote.firebase.FirebaseAuthManager
import com.imdoctor.flotilla.data.remote.firebase.FirestoreManager
import com.imdoctor.flotilla.data.remote.firebase.models.GameHistory
import com.imdoctor.flotilla.data.remote.firebase.models.GameMode
import com.imdoctor.flotilla.data.remote.firebase.models.GameResult
import com.imdoctor.flotilla.data.remote.firebase.models.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository для управления профилем пользователя и статистикой игр
 * 
 * Обеспечивает:
 * - Создание и обновление профиля
 * - Обновление статистики после игр
 * - Сохранение истории игр
 * - Получение статистики
 */
class UserRepository(
    private val firestoreManager: FirestoreManager,
    private val authManager: FirebaseAuthManager
) {
    

    // ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ
    /**
     * Создание нового профиля пользователя
     * 
     * @param nickname Никнейм пользователя
     * @return Result с профилем или ошибкой
     */
    suspend fun createUserProfile(nickname: String): Result<UserProfile> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))

            val profile = UserProfile(
                userId = userId,
                nickname = nickname,
                createdAt = null,       // ServerTimestamp установится автоматически
                lastActive = null,      // аналогично
                gamesPlayed = 0,
                wins = 0,
                losses = 0,
                totalShots = 0,
                successfulShots = 0
            )

            firestoreManager.createOrUpdateUserProfile(profile)
                .getOrThrow()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение профиля пользователя
     * 
     * @return Result с профилем или ошибкой
     */
    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            firestoreManager.getUserProfile(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Рилтайм отслеживание профиля пользователя
     * 
     * @return Flow с обновлениями профиля
     */
    fun observeUserProfile(): Flow<UserProfile?>? {
        val userId = authManager.currentUserId ?: return null
        return firestoreManager.observeUserProfile(userId)
    }
    
    /**
     * Обновление никнейма пользователя
     * 
     * @param nickname Новый никнейм
     */
    suspend fun updateNickname(nickname: String): Result<Unit> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            val currentProfile = firestoreManager.getUserProfile(userId)
                .getOrNull()
                ?: return Result.failure(Exception("Profile not found"))
            
            val updatedProfile = currentProfile.copy(nickname = nickname)
            
            firestoreManager.createOrUpdateUserProfile(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    // СТАТИСТИКА ИГР
    
    /**
     * Сохранение завершённой игры
     * 
     * Автоматически обновляет статистику пользователя
     * 
     * @param gameMode Режим игры
     * @param won Победил ли пользователь
     * @param opponentId ID противника (или "AI")
     * @param opponentNickname Никнейм противника
     * @param duration Продолжительность игры в секундах
     * @param totalShots Всего выстрелов
     * @param successfulShots Успешных выстрелов
     * @return Result с ID сохранённой игры или ошибкой
     */
    suspend fun saveCompletedGame(
        gameMode: GameMode,
        won: Boolean,
        opponentId: String,
        opponentNickname: String,
        duration: Long,
        totalShots: Int,
        successfulShots: Int
    ): Result<String> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            val gameId = UUID.randomUUID().toString()
            
            val gameHistory = GameHistory(
                gameId = gameId,
                userId = userId,
                opponentId = opponentId,
                opponentNickname = opponentNickname,
                gameMode = gameMode,
                result = if (won) GameResult.WIN else GameResult.LOSS,
                duration = duration,
                totalShots = totalShots,
                successfulShots = successfulShots
            )
            
            // Сохраняем историю игры
            firestoreManager.saveGameHistory(gameHistory)
                .getOrThrow()
            
            // Обновляем статистику пользователя
            firestoreManager.updateUserStatistics(
                userId = userId,
                won = won,
                totalShots = totalShots,
                successfulShots = successfulShots
            ).getOrThrow()
            
            Result.success(gameId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение истории игр пользователя
     * 
     * @param limit Максимальное количество игр (по умолчанию 50)
     * @return Result со списком игр или ошибкой
     */
    suspend fun getGameHistory(limit: Long = 50): Result<List<GameHistory>> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            firestoreManager.getUserGameHistory(userId, limit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Рилтайм отслеживание истории игр
     * 
     * @param limit Максимальное количество игр
     * @return Flow с обновлениями истории или null если не аутентифицирован
     */
    fun observeGameHistory(limit: Long = 50): Flow<List<GameHistory>>? {
        val userId = authManager.currentUserId ?: return null
        return firestoreManager.observeUserGameHistory(userId, limit)
    }

    // УТИЛИТЫ
    
    /**
     * Проверка, создан ли профиль пользователя
     * 
     * @return true если профиль существует
     */
    suspend fun hasProfile(): Boolean {
        val userId = authManager.currentUserId ?: return false
        return firestoreManager.getUserProfile(userId)
            .getOrNull() != null
    }
    
    /**
     * Удаление всех данных пользователя
     * 
     * НЕОБРАТИМАЯ ОПЕРАЦИЯ, ПОСКОЛЬКУ СТИРАЕМ ИЗ БД
     */
    suspend fun deleteUserData(): Result<Unit> {
        return try {
            val userId = authManager.currentUserId
                ?: return Result.failure(Exception("User not authenticated"))
            
            firestoreManager.deleteUserData(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
