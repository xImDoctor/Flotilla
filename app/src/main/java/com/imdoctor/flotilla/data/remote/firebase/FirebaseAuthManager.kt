package com.imdoctor.flotilla.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Менеджер для работы с Firebase Authentication
 * 
 * Обеспечивает:
 * - Анонимную аутентификацию пользователей
 * - Получение текущего пользователя
 * - Отслеживание состояния аутентификации
 */
class FirebaseAuthManager {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Текущий аутентифицированный пользователь
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    /**
     * ID текущего пользователя
     */
    val currentUserId: String?
        get() = currentUser?.uid
    
    /**
     * Проверка, аутентифицирован ли пользователь
     */
    val isAuthenticated: Boolean
        get() = currentUser != null
    
    /**
     * Flow для отслеживания состояния аутентификации
     */
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        
        auth.addAuthStateListener(listener)
        
        // Отправляем текущее состояние
        trySend(auth.currentUser)
        
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }
    
    /**
     * Анонимная аутентификация пользователя
     * 
     * @return Result с пользователем или ошибкой
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to sign in anonymously: user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Выход из аккаунта
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Удаление аккаунта пользователя
     *
     * ВНИМАНИЕ: Также удалит все данные пользователя из Firestore
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить Firebase ID токен для аутентификации на сервере
     *
     * Токен используется для WebSocket и REST API аутентификации
     *
     * @param forceRefresh Принудительно обновить токен (по умолчанию false)
     * @return Result с токеном или ошибкой
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> {
        return try {
            val user = currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val tokenResult = user.getIdToken(forceRefresh).await()
            val token = tokenResult.token
                ?: return Result.failure(Exception("Token is null"))

            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
