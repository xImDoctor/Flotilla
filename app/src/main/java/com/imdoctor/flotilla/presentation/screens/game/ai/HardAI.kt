package com.imdoctor.flotilla.presentation.screens.game.ai

import com.imdoctor.flotilla.presentation.screens.game.models.Board
import com.imdoctor.flotilla.presentation.screens.game.models.CellState
import com.imdoctor.flotilla.utils.Logger
import kotlin.random.Random

private const val TAG = "HardAI"

/**
 * Сложный AI противник
 *
 * Стратегия:
 * - "Читерский" AI с 70% вероятностью выстрела по кораблю (если видит корабль)
 * - После попадания гарантированно добивает корабль
 * - Использует умную стратегию добивания как у EasyAI
 * - Предоставляет серьёзный вызов даже для опытных игроков
 *
 * ВАЖНО: Этот AI "видит" корабли игрока (проверяет CellState.SHIP),
 * что делает его значительно сильнее. Но благодаря 30% случайности,
 * игрок всё ещё может победить с хорошей стратегией.
 */
class HardAI : AIOpponent {

    companion object {
        /** Вероятность выстрела по кораблю (если видим корабль) */
        private const val CHEAT_PROBABILITY = 0.7f
    }

    // История попаданий (координаты клеток, куда попали)
    private val hits = mutableListOf<Pair<Int, Int>>()

    // Очередь целей для добивания корабля
    private val targetQueue = mutableListOf<Pair<Int, Int>>()

    override suspend fun getNextMove(opponentBoard: Board): Pair<Int, Int> {
        // Если есть цели для добивания, стреляем по ним (приоритет)
        if (targetQueue.isNotEmpty()) {
            val target = targetQueue.removeAt(0)
            Logger.d(TAG, "Targeting queued position: $target")
            return target
        }

        // Если есть непотопленные корабли с попаданиями, добиваем их
        if (hits.isNotEmpty()) {
            val targetCell = findTargetAroundHits(opponentBoard)
            if (targetCell != null) {
                Logger.d(TAG, "Targeting around hits: $targetCell")
                return targetCell
            }
        }

        // Иначе используем "чит" с вероятностью 70%
        if (Random.nextFloat() < CHEAT_PROBABILITY) {
            val shipCell = findShipCell(opponentBoard)
            if (shipCell != null) {
                Logger.d(TAG, "Cheating: targeting ship at $shipCell")
                return shipCell
            }
        }

        // Если чит не сработал или кораблей не видно, стреляем случайно
        return getRandomMove(opponentBoard)
    }

    override fun notifyMoveResult(x: Int, y: Int, hit: Boolean, sunk: Boolean) {
        if (hit) {
            hits.add(Pair(x, y))
            Logger.d(TAG, "Hit at ($x, $y), sunk: $sunk. Total hits: ${hits.size}")

            if (!sunk) {
                // Корабль не потоплен, добавляем соседние клетки в очередь целей
                addAdjacentTargets(x, y)
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
     * Найти клетку с кораблём (чит)
     */
    private fun findShipCell(opponentBoard: Board): Pair<Int, Int>? {
        val shipCells = mutableListOf<Pair<Int, Int>>()

        for (x in 0..9) {
            for (y in 0..9) {
                val cell = opponentBoard.getCell(x, y)
                if (cell?.state == CellState.SHIP) {
                    shipCells.add(Pair(x, y))
                }
            }
        }

        return shipCells.randomOrNull()
    }

    /**
     * Найти цель вокруг попаданий
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

        return candidates
            .filter { (x, y) -> x in 0..9 && y in 0..9 }
            .firstOrNull { (x, y) ->
                val cell = opponentBoard.getCell(x, y)
                cell?.state == CellState.EMPTY || cell?.state == CellState.SHIP
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
        ).filter { (px, py) -> px in 0..9 && py in 0..9 }

        return adjacent.firstOrNull { (px, py) ->
            val cell = opponentBoard.getCell(px, py)
            cell?.state == CellState.EMPTY || cell?.state == CellState.SHIP
        }
    }

    /**
     * Получить случайный ход
     */
    private fun getRandomMove(opponentBoard: Board): Pair<Int, Int> {
        val availableCells = mutableListOf<Pair<Int, Int>>()

        for (x in 0..9) {
            for (y in 0..9) {
                val cell = opponentBoard.getCell(x, y)
                if (cell?.state == CellState.EMPTY || cell?.state == CellState.SHIP) {
                    availableCells.add(Pair(x, y))
                }
            }
        }

        if (availableCells.isEmpty()) {
            // Не должно случиться, но на всякий случай
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
