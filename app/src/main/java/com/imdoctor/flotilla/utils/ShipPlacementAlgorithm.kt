package com.imdoctor.flotilla.utils

import kotlin.random.Random

/**
 * Алгоритм случайной расстановки кораблей
 *
 * Генерирует корректную расстановку всех кораблей на поле 10x10:
 * - 1 линкор (4 клетки)
 * - 2 крейсера (3 клетки)
 * - 3 эсминца (2 клетки)
 * - 4 подлодки (1 клетка)
 */
object ShipPlacementAlgorithm {

    /**
     * Стандартная конфигурация кораблей: (длина, количество)
     */
    private val SHIP_COUNTS = listOf(
        4 to 1,  // 1 линкор (4 клетки)
        3 to 2,  // 2 крейсера (3 клетки)
        2 to 3,  // 3 эсминца (2 клетки)
        1 to 4   // 4 подлодки (1 клетка)
    )

    /**
     * Сгенерировать случайную расстановку кораблей
     *
     * @param maxAttempts Максимальное количество попыток генерации
     * @return Список размещённых кораблей или null, если не удалось сгенерировать
     */
    fun generateRandomPlacement(maxAttempts: Int = 10): List<ShipPlacementValidator.PlacedShip>? {
        repeat(maxAttempts) {
            val placement = tryGeneratePlacement()
            if (placement != null && placement.size == 10) {
                Logger.i("ShipPlacementAlgorithm", "Generated random placement with ${placement.size} ships")
                return placement
            }
        }

        Logger.w("ShipPlacementAlgorithm", "Failed to generate random placement after $maxAttempts attempts")
        return null
    }

    /**
     * Попытаться сгенерировать полную расстановку
     */
    private fun tryGeneratePlacement(): List<ShipPlacementValidator.PlacedShip>? {
        val ships = mutableListOf<ShipPlacementValidator.PlacedShip>()
        var shipId = 0

        // Размещаем корабли от больших к маленьким (проще найти место для больших)
        for ((length, count) in SHIP_COUNTS) {
            repeat(count) {
                val ship = placeShipRandomly(length, ships, shipId++)
                if (ship == null) {
                    // Не удалось разместить корабль, начинаем заново
                    return null
                }
                ships.add(ship)
            }
        }

        return ships
    }

    /**
     * Разместить корабль случайным образом
     *
     * @param length Длина корабля
     * @param existingShips Уже размещённые корабли
     * @param shipId ID нового корабля
     * @param maxAttempts Максимальное количество попыток размещения
     * @return Размещённый корабль или null, если не удалось разместить
     */
    private fun placeShipRandomly(
        length: Int,
        existingShips: List<ShipPlacementValidator.PlacedShip>,
        shipId: Int,
        maxAttempts: Int = 100
    ): ShipPlacementValidator.PlacedShip? {
        repeat(maxAttempts) {
            val x = Random.nextInt(10)
            val y = Random.nextInt(10)
            val orientation = if (Random.nextBoolean()) {
                ShipPlacementValidator.Orientation.HORIZONTAL
            } else {
                ShipPlacementValidator.Orientation.VERTICAL
            }

            val validation = ShipPlacementValidator.validatePlacement(
                x, y, length, orientation, existingShips
            )

            if (validation is ShipPlacementValidator.ValidationResult.Valid) {
                return ShipPlacementValidator.PlacedShip(
                    id = "ship_$shipId",
                    length = length,
                    x = x,
                    y = y,
                    orientation = orientation
                )
            }
        }

        return null
    }
}
