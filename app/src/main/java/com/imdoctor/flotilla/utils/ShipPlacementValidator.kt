package com.imdoctor.flotilla.utils

/**
 * Валидатор расстановки кораблей
 *
 * Проверяет корректность размещения кораблей на игровом поле 10x10:
 * - Корабли не выходят за границы поля
 * - Корабли не пересекаются
 * - Корабли не касаются друг друга (даже по диагонали)
 */
object ShipPlacementValidator {

    /**
     * Результат валидации
     */
    sealed class ValidationResult {
        /** Расстановка корректна */
        object Valid : ValidationResult()

        /** Расстановка некорректна */
        data class Invalid(val reason: String) : ValidationResult()
    }

    /**
     * Размещённый корабль
     */
    data class PlacedShip(
        val id: String,
        val length: Int,
        val x: Int,
        val y: Int,
        val orientation: Orientation
    )

    /**
     * Ориентация корабля
     */
    enum class Orientation {
        HORIZONTAL, VERTICAL;

        /** Переключить ориентацию */
        fun toggle() = if (this == HORIZONTAL) VERTICAL else HORIZONTAL
    }

    /**
     * Проверить корректность размещения корабля
     *
     * @param x Координата X начала корабля (0-9)
     * @param y Координата Y начала корабля (0-9)
     * @param length Длина корабля (1-4)
     * @param orientation Ориентация (горизонтальная/вертикальная)
     * @param existingShips Уже размещённые корабли
     * @return ValidationResult.Valid если размещение корректно, иначе Invalid с причиной
     */
    fun validatePlacement(
        x: Int,
        y: Int,
        length: Int,
        orientation: Orientation,
        existingShips: List<PlacedShip>
    ): ValidationResult {
        // Проверка границ поля
        if (!isWithinBounds(x, y, length, orientation)) {
            return ValidationResult.Invalid("Корабль выходит за границы поля")
        }

        val positions = getAllPositions(x, y, length, orientation)

        // Проверка пересечения с другими кораблями
        if (hasOverlap(positions, existingShips)) {
            return ValidationResult.Invalid("Корабли не могут пересекаться")
        }

        // Проверка касания других кораблей
        if (hasAdjacency(positions, existingShips)) {
            return ValidationResult.Invalid("Корабли не могут касаться друг друга")
        }

        return ValidationResult.Valid
    }

    /**
     * Проверить, что корабль находится в границах поля 10x10
     */
    fun isWithinBounds(x: Int, y: Int, length: Int, orientation: Orientation): Boolean {
        return when (orientation) {
            Orientation.HORIZONTAL -> x in 0..9 && y in 0..9 && x + length - 1 <= 9
            Orientation.VERTICAL -> x in 0..9 && y in 0..9 && y + length - 1 <= 9
        }
    }

    /**
     * Проверить, пересекается ли корабль с уже размещёнными
     */
    fun hasOverlap(positions: List<Pair<Int, Int>>, existingShips: List<PlacedShip>): Boolean {
        val existingPositions = existingShips.flatMap { ship ->
            getAllPositions(ship.x, ship.y, ship.length, ship.orientation)
        }.toSet()

        return positions.any { it in existingPositions }
    }

    /**
     * Проверить, касается ли корабль других кораблей (включая диагонали)
     */
    fun hasAdjacency(positions: List<Pair<Int, Int>>, existingShips: List<PlacedShip>): Boolean {
        val existingPositions = existingShips.flatMap { ship ->
            getAllPositions(ship.x, ship.y, ship.length, ship.orientation)
        }.toSet()

        // Проверить все соседние клетки для каждой позиции корабля
        val adjacentCells = positions.flatMap { (x, y) ->
            getAdjacentCells(x, y)
        }.toSet()

        return adjacentCells.any { it in existingPositions }
    }

    /**
     * Получить все позиции, занимаемые кораблём
     */
    fun getAllPositions(x: Int, y: Int, length: Int, orientation: Orientation): List<Pair<Int, Int>> {
        return when (orientation) {
            Orientation.HORIZONTAL -> (x until x + length).map { Pair(it, y) }
            Orientation.VERTICAL -> (y until y + length).map { Pair(x, it) }
        }
    }

    /**
     * Получить все соседние клетки (8 направлений + сама клетка)
     */
    private fun getAdjacentCells(x: Int, y: Int): List<Pair<Int, Int>> {
        return listOf(
            Pair(x - 1, y - 1), Pair(x, y - 1), Pair(x + 1, y - 1),
            Pair(x - 1, y), Pair(x + 1, y),
            Pair(x - 1, y + 1), Pair(x, y + 1), Pair(x + 1, y + 1)
        ).filter { (px, py) -> px in 0..9 && py in 0..9 }
    }
}
