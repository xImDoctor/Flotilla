package com.imdoctor.flotilla.presentation.screens.game.ai

import com.imdoctor.flotilla.presentation.screens.game.models.Board

/**
 * Интерфейс для AI противника
 *
 * Определяет методы, которые должен реализовать любой AI противник
 */
interface AIOpponent {
    /**
     * Получить следующий ход AI
     *
     * @param opponentBoard Игровое поле противника (видимое AI)
     * @return Пара (x, y) координат для следующего выстрела
     */
    suspend fun getNextMove(opponentBoard: Board): Pair<Int, Int>

    /**
     * Уведомить AI о результате предыдущего хода
     *
     * @param x Координата X выстрела
     * @param y Координата Y выстрела
     * @param hit Попадание ли это было
     * @param sunk Потоплен ли корабль
     */
    fun notifyMoveResult(x: Int, y: Int, hit: Boolean, sunk: Boolean)
}

/**
 * Уровень сложности AI
 */
enum class AIDifficulty {
    /** Лёгкий уровень - умный случайный AI */
    EASY,

    /** Сложный уровень - AI с "читами" (видит корабли с вероятностью) */
    HARD
}
