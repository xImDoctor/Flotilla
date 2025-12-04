package com.imdoctor.flotilla.presentation.screens.game.models

/**
 * Состояние клетки на игровом поле
 */
enum class CellState {
    EMPTY,      // Пустая клетка
    SHIP,       // Клетка с кораблём (не подбитая)
    HIT,        // Попадание
    MISS,       // Промах
    SUNK        // Потопленный корабль
}

/**
 * Клетка игрового поля
 *
 * @property x Координата X (0-9)
 * @property y Координата Y (0-9)
 * @property state Текущее состояние клетки
 */
data class Cell(
    val x: Int,
    val y: Int,
    val state: CellState = CellState.EMPTY
)

/**
 * Корабль на игровом поле
 *
 * @property id Уникальный идентификатор корабля
 * @property length Длина корабля (1-4)
 * @property positions Список координат клеток корабля
 * @property hits Набор координат попаданий по кораблю
 */
data class Ship(
    val id: String,
    val length: Int,
    val positions: List<Pair<Int, Int>>,
    val hits: Set<Pair<Int, Int>> = emptySet()
) {
    /**
     * Проверяет, потоплен ли корабль
     */
    val isSunk: Boolean
        get() = hits.size == positions.size

    /**
     * Проверяет, находится ли указанная координата в корабле
     */
    fun containsPosition(x: Int, y: Int): Boolean {
        return positions.contains(Pair(x, y))
    }

    /**
     * Проверяет, подбита ли указанная координата
     */
    fun isHit(x: Int, y: Int): Boolean {
        return hits.contains(Pair(x, y))
    }
}

/**
 * Игровое поле 10x10
 *
 * @property cells Двумерный массив клеток [x][y]
 * @property ships Список кораблей на поле
 */
data class Board(
    val cells: Array<Array<Cell>> = Array(10) { x ->
        Array(10) { y ->
            Cell(x, y)
        }
    },
    val ships: List<Ship> = emptyList()
) {
    /**
     * Получить клетку по координатам
     */
    fun getCell(x: Int, y: Int): Cell? {
        if (x !in 0..9 || y !in 0..9) return null
        return cells[x][y]
    }

    /**
     * Обновить состояние клетки
     */
    fun updateCell(x: Int, y: Int, state: CellState): Board {
        if (x !in 0..9 || y !in 0..9) return this

        val newCells = cells.map { it.clone() }.toTypedArray()
        newCells[x][y] = Cell(x, y, state)

        return copy(cells = newCells)
    }

    /**
     * Получить корабль по координатам клетки
     */
    fun getShipAt(x: Int, y: Int): Ship? {
        return ships.find { it.containsPosition(x, y) }
    }

    /**
     * Обновить корабль (добавить попадание)
     */
    fun updateShip(shipId: String, hit: Pair<Int, Int>): Board {
        val newShips = ships.map { ship ->
            if (ship.id == shipId) {
                ship.copy(hits = ship.hits + hit)
            } else {
                ship
            }
        }
        return copy(ships = newShips)
    }

    /**
     * Проверяет, все ли корабли потоплены
     */
    val allShipsSunk: Boolean
        get() = ships.isNotEmpty() && ships.all { it.isSunk }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        if (!cells.contentDeepEquals(other.cells)) return false
        if (ships != other.ships) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cells.contentDeepHashCode()
        result = 31 * result + ships.hashCode()
        return result
    }
}

/**
 * Состояние онлайн игры
 *
 * @property gameId Идентификатор игры
 * @property opponentNickname Никнейм противника
 * @property myBoard Моё игровое поле (с моими кораблями)
 * @property opponentBoard Поле противника (для атак)
 * @property isMyTurn Мой ли сейчас ход
 * @property gameOver Завершена ли игра
 * @property winner Победитель (null если игра не завершена)
 */
data class GameState(
    val gameId: String,
    val opponentNickname: String,
    val myBoard: Board,
    val opponentBoard: Board,
    val isMyTurn: Boolean,
    val gameOver: Boolean = false,
    val winner: String? = null
)
