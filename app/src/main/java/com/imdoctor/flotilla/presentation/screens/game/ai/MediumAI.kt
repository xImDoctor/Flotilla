package com.imdoctor.flotilla.presentation.screens.game.ai

import com.imdoctor.flotilla.presentation.screens.game.models.Board
import com.imdoctor.flotilla.presentation.screens.game.models.CellState
import com.imdoctor.flotilla.utils.Logger
import kotlin.random.Random

private const val TAG = "MediumAI"

/**
 * Средний AI противник
 *
 * Стратегия: Parity Targeting (шахматная доска)
 * - При поиске кораблей стреляет только по клеткам в шахматном порядке
 * - Гарантирует попадание в любой корабль (т.к. корабль 2+ клеток обязательно попадёт на "шахматную" клетку)
 * - С 10% вероятностью пропускает parity паттерн для добавления случайности
 * - После попадания использует умную логику добивания как у EasyAI
 * - Честная стратегия без читерства, но эффективнее случайных выстрелов
 *
 * Эффективность: ~50% выстрелов тратится на поиск (против 100% у EasyAI),
 * но гарантированно попадает во все корабли.
 */
class MediumAI : AIOpponent {

    companion object {
        /** Вероятность пропуска parity паттерна (добавление случайности) */
        private const val RANDOMNESS_PROBABILITY = 0.1f  // 10%
    }

    // История попаданий (координаты клеток, куда попали)
    private val hits = mutableListOf<Pair<Int, Int>>()

    // Очередь целей для добивания корабля
    private val targetQueue = mutableListOf<Pair<Int, Int>>()

    /**
     * Проверить, доступна ли клетка для атаки
     * (не атакована ранее: не HIT, не MISS, не SUNK)
     */
    private fun isCellAvailableForAttack(x: Int, y: Int, opponentBoard: Board): Boolean {
        if (x !in 0..9 || y !in 0..9) return false
        val cell = opponentBoard.getCell(x, y) ?: return false
        return cell.state == CellState.EMPTY || cell.state == CellState.SHIP
    }

    override suspend fun getNextMove(opponentBoard: Board): Pair<Int, Int> {
        // Если есть цели для добивания, стреляем по ним (приоритет)
        // Фильтруем уже атакованные клетки
        while (targetQueue.isNotEmpty()) {
            val target = targetQueue.removeAt(0)
            if (isCellAvailableForAttack(target.first, target.second, opponentBoard)) {
                Logger.d(TAG, "Targeting queued position: $target")
                return target
            }
        }

        // Если есть непотопленные корабли с попаданиями, добиваем их
        if (hits.isNotEmpty()) {
            val targetCell = findTargetAroundHits(opponentBoard)
            if (targetCell != null) {
                Logger.d(TAG, "Targeting around hits: $targetCell")
                return targetCell
            }
        }

        // С вероятностью 10% игнорировать parity паттерн
        if (Random.nextFloat() < RANDOMNESS_PROBABILITY) {
            Logger.d(TAG, "Random shot (10% chance)")
            return getRandomMove(opponentBoard)
        }

        // Иначе используем parity targeting (шахматная доска)
        return getParityMove(opponentBoard)
    }

    override fun notifyMoveResult(x: Int, y: Int, hit: Boolean, sunk: Boolean) {
        if (hit) {
            hits.add(Pair(x, y))
            Logger.d(TAG, "Hit at ($x, $y), sunk: $sunk. Total hits: ${hits.size}")

            if (!sunk) {
                // Корабль не потоплен
                if (hits.size == 1) {
                    // Первое попадание - добавляем все 4 соседние клетки
                    addAdjacentTargets(x, y)
                } else {
                    // 2+ попадания - направление известно, очищаем targetQueue
                    // findTargetAroundHits сам найдет правильные клетки вдоль линии
                    targetQueue.clear()
                    Logger.d(TAG, "Direction known, cleared targetQueue")
                }
            } else {
                // Корабль потоплен, очищаем историю попаданий и очередь целей
                Logger.d(TAG, "Ship sunk! Clearing targets")
                hits.clear()
                targetQueue.clear()
            }
        } else {
            Logger.d(TAG, "Miss at ($x, $y)")
        }
    }

    /**
     * Проверить, является ли клетка "шахматной"
     *
     * Клетки с (x + y) % 2 == 0 образуют шахматный паттерн,
     * покрывающий половину доски и гарантирующий попадание в любой корабль длиной ≥2
     */
    private fun isParityCell(x: Int, y: Int): Boolean {
        return (x + y) % 2 == 0
    }

    /**
     * Получить ход по стратегии parity targeting
     */
    private fun getParityMove(opponentBoard: Board): Pair<Int, Int> {
        val availableParityCells = mutableListOf<Pair<Int, Int>>()

        for (x in 0..9) {
            for (y in 0..9) {
                if (isCellAvailableForAttack(x, y, opponentBoard) && isParityCell(x, y)) {
                    availableParityCells.add(Pair(x, y))
                }
            }
        }

        // Если нет доступных parity клеток (теоретически не должно случиться),
        // возвращаемся к случайной стратегии
        if (availableParityCells.isEmpty()) {
            Logger.w(TAG, "No parity cells available, falling back to random")
            return getRandomMove(opponentBoard)
        }

        val randomParityCell = availableParityCells.random()
        Logger.d(TAG, "Parity move: $randomParityCell (${availableParityCells.size} parity cells available)")
        return randomParityCell
    }

    /**
     * Найти цель вокруг попаданий (идентично EasyAI)
     */
    private fun findTargetAroundHits(opponentBoard: Board): Pair<Int, Int>? {
        // Если есть 2+ попадания, попробовать определить направление
        if (hits.size >= 2) {
            val direction = detectShipDirection()
            if (direction != null) {
                Logger.d(TAG, "Detected ship direction: $direction")
                return findTargetInDirection(opponentBoard, direction)
            }
        }

        // Иначе стреляем вокруг первого попадания
        if (hits.isNotEmpty()) {
            val (x, y) = hits.first()
            return getAdjacentEmptyCell(x, y, opponentBoard)
        }

        return null
    }

    /**
     * Определить направление корабля по попаданиям
     */
    private fun detectShipDirection(): Direction? {
        if (hits.size < 2) return null

        val first = hits.first()
        val second = hits.last()

        return when {
            first.first == second.first -> Direction.VERTICAL
            first.second == second.second -> Direction.HORIZONTAL
            else -> null
        }
    }

    /**
     * Найти цель в направлении корабля
     */
    private fun findTargetInDirection(opponentBoard: Board, direction: Direction): Pair<Int, Int>? {
        val sortedHits = when (direction) {
            Direction.HORIZONTAL -> hits.sortedBy { it.first }
            Direction.VERTICAL -> hits.sortedBy { it.second }
        }

        val firstHit = sortedHits.first()
        val lastHit = sortedHits.last()

        // Проверить клетки в начале и конце линии
        val candidates = when (direction) {
            Direction.HORIZONTAL -> listOf(
                Pair(firstHit.first - 1, firstHit.second),
                Pair(lastHit.first + 1, lastHit.second)
            )
            Direction.VERTICAL -> listOf(
                Pair(firstHit.first, firstHit.second - 1),
                Pair(lastHit.first, lastHit.second + 1)
            )
        }

        return candidates.firstOrNull { (x, y) ->
            isCellAvailableForAttack(x, y, opponentBoard)
        }
    }

    /**
     * Добавить соседние клетки в очередь целей
     */
    private fun addAdjacentTargets(x: Int, y: Int) {
        val adjacent = listOf(
            Pair(x - 1, y),
            Pair(x + 1, y),
            Pair(x, y - 1),
            Pair(x, y + 1)
        ).filter { (px, py) -> px in 0..9 && py in 0..9 }

        targetQueue.addAll(adjacent)
    }

    /**
     * Получить соседнюю пустую клетку
     */
    private fun getAdjacentEmptyCell(x: Int, y: Int, opponentBoard: Board): Pair<Int, Int>? {
        val adjacent = listOf(
            Pair(x - 1, y),
            Pair(x + 1, y),
            Pair(x, y - 1),
            Pair(x, y + 1)
        )

        return adjacent.firstOrNull { (px, py) ->
            isCellAvailableForAttack(px, py, opponentBoard)
        }
    }

    /**
     * Получить случайный ход (fallback)
     */
    private fun getRandomMove(opponentBoard: Board): Pair<Int, Int> {
        val availableCells = mutableListOf<Pair<Int, Int>>()

        for (x in 0..9) {
            for (y in 0..9) {
                if (isCellAvailableForAttack(x, y, opponentBoard)) {
                    availableCells.add(Pair(x, y))
                }
            }
        }

        if (availableCells.isEmpty()) {
            Logger.e(TAG, "No available cells!")
            return Pair(0, 0)
        }

        val randomCell = availableCells.random()
        Logger.d(TAG, "Random move: $randomCell (${availableCells.size} available cells)")
        return randomCell
    }

    /**
     * Направление корабля
     */
    private enum class Direction {
        HORIZONTAL, VERTICAL
    }
}
