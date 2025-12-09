package com.imdoctor.flotilla.presentation.screens.shipsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imdoctor.flotilla.data.remote.websocket.models.ShipPlacement
import com.imdoctor.flotilla.data.repository.SettingsRepository
import com.imdoctor.flotilla.utils.Logger
import com.imdoctor.flotilla.utils.ShipPlacementAlgorithm
import com.imdoctor.flotilla.utils.ShipPlacementValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ShipSetupViewModel"

/**
 * ViewModel для экрана расстановки кораблей
 *
 * Управляет состоянием расстановки, валидацией и взаимодействием с UI
 */
class ShipSetupViewModel(
    private val gameMode: String,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipSetupUiState())
    val uiState: StateFlow<ShipSetupUiState> = _uiState.asStateFlow()

    val showCoordinates: StateFlow<Boolean> = settingsRepository.showCoordinatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val animationsEnabled: StateFlow<Boolean> = settingsRepository.animationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /**
     * Разместить корабль на поле
     *
     * @param shipTemplate Шаблон корабля (тип)
     * @param x Координата X начала корабля
     * @param y Координата Y начала корабля
     * @param orientation Ориентация корабля
     */
    fun placeShip(
        shipTemplate: ShipTemplate,
        x: Int,
        y: Int,
        orientation: ShipPlacementValidator.Orientation
    ) {
        val currentState = _uiState.value

        // Проверить, что у нас есть доступные корабли этого типа
        val template = currentState.availableShips.find { it.id == shipTemplate.id }
        if (template == null || template.placed >= template.count) {
            _uiState.value = currentState.copy(
                errorMessage = "Все корабли этого типа уже размещены"
            )
            return
        }

        // Валидировать размещение
        val validation = ShipPlacementValidator.validatePlacement(
            x, y, shipTemplate.length, orientation, currentState.placedShips.values.toList()
        )

        if (validation is ShipPlacementValidator.ValidationResult.Invalid) {
            _uiState.value = currentState.copy(errorMessage = validation.reason)
            Logger.w(TAG, "Invalid ship placement: ${validation.reason}")
            return
        }

        // Разместить корабль
        val shipId = "ship_${shipTemplate.id}_${template.placed}"
        val placedShip = ShipPlacementValidator.PlacedShip(
            id = shipId,
            length = shipTemplate.length,
            x = x,
            y = y,
            orientation = orientation
        )

        val updatedPlacedShips = currentState.placedShips + (shipId to placedShip)
        val updatedTemplates = currentState.availableShips.map { t ->
            if (t.id == shipTemplate.id) t.copy(placed = t.placed + 1) else t
        }

        _uiState.value = currentState.copy(
            placedShips = updatedPlacedShips,
            availableShips = updatedTemplates,
            isValid = isPlacementComplete(updatedTemplates),
            errorMessage = null
        )

        Logger.i(TAG, "Ship placed: $shipId at ($x, $y) ${orientation.name}")
    }

    /**
     * Удалить корабль с поля
     *
     * @param shipId ID корабля
     */
    fun removeShip(shipId: String) {
        val currentState = _uiState.value
        val ship = currentState.placedShips[shipId] ?: return

        val updatedPlacedShips = currentState.placedShips - shipId

        // Определить тип корабля из ID (формат: ship_TYPE_INDEX)
        val templateId = shipId.substringAfter("ship_").substringBefore("_")
        val updatedTemplates = currentState.availableShips.map { t ->
            if (t.id == templateId) t.copy(placed = t.placed - 1) else t
        }

        _uiState.value = currentState.copy(
            placedShips = updatedPlacedShips,
            availableShips = updatedTemplates,
            isValid = false,
            errorMessage = null
        )

        Logger.i(TAG, "Ship removed: $shipId")
    }

    /**
     * Повернуть корабль на 90 градусов
     *
     * @param shipId ID корабля
     */
    fun rotateShip(shipId: String) {
        val currentState = _uiState.value
        val ship = currentState.placedShips[shipId] ?: return

        val newOrientation = ship.orientation.toggle()
        val otherShips = currentState.placedShips.values.filter { it.id != shipId }

        // Валидировать новую ориентацию
        val validation = ShipPlacementValidator.validatePlacement(
            ship.x, ship.y, ship.length, newOrientation, otherShips
        )

        if (validation is ShipPlacementValidator.ValidationResult.Invalid) {
            _uiState.value = currentState.copy(errorMessage = validation.reason)
            Logger.w(TAG, "Cannot rotate ship: ${validation.reason}")
            return
        }

        val rotatedShip = ship.copy(orientation = newOrientation)
        val updatedPlacedShips = currentState.placedShips + (shipId to rotatedShip)

        _uiState.value = currentState.copy(
            placedShips = updatedPlacedShips,
            errorMessage = null
        )

        Logger.i(TAG, "Ship rotated: $shipId to ${newOrientation.name}")
    }

    /**
     * Случайная расстановка всех кораблей
     */
    fun randomPlacement() {
        viewModelScope.launch {
            Logger.i(TAG, "Generating random ship placement")

            val randomShips = ShipPlacementAlgorithm.generateRandomPlacement()

            if (randomShips == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Не удалось сгенерировать расстановку. Попробуйте снова."
                )
                Logger.e(TAG, "Failed to generate random placement")
                return@launch
            }

            // Обновить счётчики шаблонов
            val templates = ShipTemplate.defaultTemplates().map { template ->
                val placedCount = randomShips.count { it.length == template.length }
                template.copy(placed = placedCount)
            }

            _uiState.value = ShipSetupUiState(
                availableShips = templates,
                placedShips = randomShips.associateBy { it.id },
                isValid = true,
                errorMessage = null
            )

            Logger.i(TAG, "Random placement generated: ${randomShips.size} ships")
        }
    }

    /**
     * Очистить все корабли с поля
     */
    fun clearAll() {
        _uiState.value = ShipSetupUiState()
        Logger.i(TAG, "All ships cleared")
    }

    /**
     * Валидировать и получить расстановку для игры
     *
     * @return Список размещённых кораблей для отправки на сервер или null если невалидно
     */
    fun validateAndProceed(): List<ShipPlacement>? {
        val currentState = _uiState.value
        if (!currentState.isValid) {
            Logger.w(TAG, "Cannot proceed: placement is not valid")
            return null
        }

        val shipPlacements = currentState.placedShips.values.map { ship ->
            ShipPlacement(
                x = ship.x,
                y = ship.y,
                length = ship.length,
                orientation = when (ship.orientation) {
                    ShipPlacementValidator.Orientation.HORIZONTAL -> "horizontal"
                    ShipPlacementValidator.Orientation.VERTICAL -> "vertical"
                }
            )
        }

        Logger.i(TAG, "Ship placement validated: ${shipPlacements.size} ships")
        return shipPlacements
    }

    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Проверить, что все корабли размещены
     */
    private fun isPlacementComplete(templates: List<ShipTemplate>): Boolean {
        return templates.all { it.placed == it.count }
    }
}

/**
 * UI состояние экрана расстановки кораблей
 */
data class ShipSetupUiState(
    /** Доступные шаблоны кораблей */
    val availableShips: List<ShipTemplate> = ShipTemplate.defaultTemplates(),

    /** Размещённые корабли (ID -> корабль) */
    val placedShips: Map<String, ShipPlacementValidator.PlacedShip> = emptyMap(),

    /** Расстановка валидна (все корабли размещены корректно) */
    val isValid: Boolean = false,

    /** Сообщение об ошибке */
    val errorMessage: String? = null
)

/**
 * Шаблон корабля
 *
 * Определяет тип корабля и количество доступных для размещения
 */
data class ShipTemplate(
    /** ID типа корабля */
    val id: String,

    /** Длина корабля в клетках */
    val length: Int,

    /** Общее количество кораблей этого типа */
    val count: Int,

    /** Количество уже размещённых кораблей этого типа */
    val placed: Int = 0
) {
    companion object {
        /**
         * Стандартная конфигурация кораблей для Морского боя
         */
        fun defaultTemplates() = listOf(
            ShipTemplate("battleship", 4, 1),  // 1 линкор (4 клетки)
            ShipTemplate("cruiser", 3, 2),     // 2 крейсера (3 клетки)
            ShipTemplate("destroyer", 2, 3),   // 3 эсминца (2 клетки)
            ShipTemplate("submarine", 1, 4)    // 4 подлодки (1 клетка)
        )
    }
}
