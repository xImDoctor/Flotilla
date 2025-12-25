package com.imdoctor.flotilla.presentation.screens.shipsetup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imdoctor.flotilla.R
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.presentation.theme.FlotillaColors
import com.imdoctor.flotilla.utils.ShipPlacementValidator
import kotlin.math.roundToInt

/**
 * Адаптивные размеры для ShipSetupScreen
 */
private object ShipSetupDimens {
    @Composable
    fun gridAspectRatio(): Float {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        // Автоматический выбор пропорций на основе высоты экрана
        return when {
            screenHeight < 700 -> 0.9f  // Более квадратная сетка для маленьких экранов
            screenHeight < 800 -> 0.85f
            else -> 0.8f  // Вытянутая сетка для больших экранов
        }
    }

    @Composable
    fun spacerSmall(): Dp {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        return if (screenHeight < 700) 6.dp else 8.dp
    }

    @Composable
    fun spacerMedium(): Dp {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        return if (screenHeight < 700) 11.dp else 16.dp
    }

    @Composable
    fun spacerLarge(): Dp {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        return if (screenHeight < 700) 16.dp else 24.dp
    }
}

/**
 * Экран расстановки кораблей с drag-and-drop интерфейсом
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipSetupScreen(
    gameMode: String,
    onSetupComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ShipSetupViewModel = viewModel(
        factory = ShipSetupViewModelFactory(gameMode)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCoordinates by viewModel.showCoordinates.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()

    // Состояние перетаскивания
    var draggedShip by remember { mutableStateOf<ShipTemplate?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragOrientation by remember { mutableStateOf(ShipPlacementValidator.Orientation.HORIZONTAL) }

    // Позиции элементов для расчёта drag-and-drop
    var gridPosition by remember { mutableStateOf(Offset.Zero) }
    var cellSize by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ship_setup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FlotillaColors.Background,
                    titleContentColor = FlotillaColors.OnBackground
                )
            )
        },
        containerColor = FlotillaColors.Background,
        snackbarHost = {
            // Показ ошибок
            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = stringResource(R.string.ship_setup_heading),
                    style = MaterialTheme.typography.headlineSmall,
                    color = FlotillaColors.OnBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(ShipSetupDimens.spacerSmall()))

                // Палитра кораблей
                ShipPalette(
                    templates = uiState.availableShips,
                    onShipDragStart = { template, orientation ->
                        draggedShip = template
                        dragOrientation = orientation
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ShipSetupDimens.spacerMedium()))

                // Игровое поле с drag overlay
                Box(modifier = Modifier.fillMaxWidth()) {
                    GameGrid(
                        placedShips = uiState.placedShips,
                        showCoordinates = showCoordinates,
                        onShipTap = { shipId ->
                            viewModel.rotateShip(shipId)
                        },
                        onShipRemove = { shipId ->
                            viewModel.removeShip(shipId)
                        },
                        onPositionUpdate = { position, size ->
                            gridPosition = position
                            cellSize = size
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ShipSetupDimens.gridAspectRatio())
                    )

                    // Drag overlay - ТОЛЬКО над grid area
                    if (draggedShip != null) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()  // Размер grid Box, не fillMaxSize
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            dragOffset = offset
                                        },
                                        onDrag = { _, delta ->
                                            dragOffset += delta
                                        },
                                        onDragEnd = {
                                            // Координаты относительно grid (уже учтены header'ы в gridPosition)
                                            val relativeX = dragOffset.x
                                            val relativeY = dragOffset.y

                                            val cellX = (relativeX / cellSize).toInt()
                                            val cellY = (relativeY / cellSize).toInt()

                                            if (cellX in 0..9 && cellY in 0..9 && draggedShip != null) {
                                                viewModel.placeShip(
                                                    draggedShip!!,
                                                    cellX,
                                                    cellY,
                                                    dragOrientation
                                                )
                                            }

                                            draggedShip = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggedShip = null
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                }
                        ) {
                            // Preview внутри drag box
                            draggedShip?.let { ship ->
                                DraggedShipPreview(
                                    ship = ship,
                                    orientation = dragOrientation,
                                    offset = dragOffset,
                                    cellSize = cellSize
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ShipSetupDimens.spacerLarge()))

                // Кнопки управления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка случайной расстановки
                    OutlinedButton(
                        onClick = { viewModel.randomPlacement() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FlotillaColors.Primary
                        )
                    ) {
                        Text("Случайно")
                    }

                    // Кнопка очистки
                    OutlinedButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.placedShips.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FlotillaColors.Error
                        )
                    ) {
                        Text("Очистить")
                    }
                }

                Spacer(modifier = Modifier.height(ShipSetupDimens.spacerMedium()))

                // Кнопка готовности
                Button(
                    onClick = {
                        val ships = viewModel.validateAndProceed()
                        if (ships != null) {
                            val gameId = "game_${System.currentTimeMillis()}"

                            // Для AI режимов сохраняем данные в AIGameDataHolder
                            if (gameMode.startsWith("ai_")) {
                                com.imdoctor.flotilla.data.repository.AIGameDataHolder.saveGameData(
                                    gameId,
                                    com.imdoctor.flotilla.data.repository.AIGameDataHolder.AIGameData(
                                        gameId = gameId,
                                        playerShips = ships,
                                        difficulty = gameMode
                                    )
                                )
                            }

                            // Для online режима сохраняем корабли для матчмейкинга
                            if (gameMode == "online") {
                                com.imdoctor.flotilla.data.repository.MatchmakingDataHolder.savePreparedShips(ships)
                            }

                            // Переход к игре или матчмейкингу
                            onSetupComplete(gameId)
                        }
                    },
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlotillaColors.Primary,
                        contentColor = FlotillaColors.OnPrimary,
                        disabledContainerColor = FlotillaColors.SurfaceVariant,
                        disabledContentColor = FlotillaColors.OnSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (uiState.isValid) "Готов к бою!" else "Разместите все корабли",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Палитра доступных кораблей для размещения
 */
@Composable
private fun ShipPalette(
    templates: List<ShipTemplate>,
    onShipDragStart: (ShipTemplate, ShipPlacementValidator.Orientation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FlotillaColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Корабли для размещения",
                style = MaterialTheme.typography.titleSmall,
                color = FlotillaColors.OnSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            templates.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Визуализация корабля
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Иконка корабля (квадраты)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(template.length) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            color = if (template.placed < template.count) {
                                                FlotillaColors.Primary
                                            } else {
                                                FlotillaColors.SurfaceVariant
                                            },
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = FlotillaColors.Outline,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        Text(
                            text = "${template.placed}/${template.count}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (template.placed < template.count) {
                                FlotillaColors.OnSurface
                            } else {
                                FlotillaColors.OnSurfaceVariant
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Кнопка взятия корабля
                    if (template.placed < template.count) {
                        OutlinedButton(
                            onClick = {
                                onShipDragStart(template, ShipPlacementValidator.Orientation.HORIZONTAL)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = FlotillaColors.Primary
                            )
                        ) {
                            Text("Взять", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Игровая сетка 10x10 с размещёнными кораблями
 */
@Composable
private fun GameGrid(
    placedShips: Map<String, ShipPlacementValidator.PlacedShip>,
    showCoordinates: Boolean,
    onShipTap: (String) -> Unit,
    onShipRemove: (String) -> Unit,
    onPositionUpdate: (Offset, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FlotillaColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val totalSize = with(density) { maxWidth.toPx() }
            val headerSize = if (showCoordinates) 24.dp else 0.dp
            val headerSizePx = with(density) { headerSize.toPx() }
            val gridSize = totalSize - headerSizePx
            val cellSizePx = gridSize / 10f
            val cellSizeDp = with(density) { cellSizePx.toDp() }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row с буквами столбцов (A-J)
                if (showCoordinates) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(headerSize))  // Угловая пустая клетка

                        for (x in 0..9) {
                            Box(
                                modifier = Modifier.size(cellSizeDp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + x).toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FlotillaColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Строки с номерами и клетками
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            // НЕ корректируем position - он уже правильный из layout system
                            onPositionUpdate(position, cellSizePx)
                        }
                ) {
                    // Фон сетки
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cellSizeDp * 10)
                            .background(FlotillaColors.Background)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Левая колонка с номерами строк
                        if (showCoordinates) {
                            Column {
                                for (y in 0..9) {
                                    Box(
                                        modifier = Modifier.size(headerSize, cellSizeDp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (y + 1).toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FlotillaColors.OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Игровая сетка 10x10
                        Column {
                            for (y in 0..9) {
                                Row {
                                    for (x in 0..9) {
                                        GridCell(
                                            modifier = Modifier.size(cellSizeDp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Размещённые корабли (overlay)
                    if (showCoordinates) {
                        Spacer(modifier = Modifier.width(headerSize))
                    }
                    Box(modifier = Modifier.size(cellSizeDp * 10)) {
                        placedShips.forEach { (shipId, ship) ->
                            PlacedShipVisual(
                                ship = ship,
                                cellSize = cellSizeDp,
                                onTap = { onShipTap(shipId) },
                                onLongPress = { onShipRemove(shipId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Одна клетка сетки (без координат внутри)
 */
@Composable
private fun GridCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(0.5.dp, FlotillaColors.Outline)
    )
}

/**
 * Визуализация размещённого корабля
 */
@Composable
private fun PlacedShipVisual(
    ship: ShipPlacementValidator.PlacedShip,
    cellSize: Dp,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val width = when (ship.orientation) {
        ShipPlacementValidator.Orientation.HORIZONTAL -> cellSize.times(ship.length)
        ShipPlacementValidator.Orientation.VERTICAL -> cellSize
    }

    val height = when (ship.orientation) {
        ShipPlacementValidator.Orientation.HORIZONTAL -> cellSize
        ShipPlacementValidator.Orientation.VERTICAL -> cellSize.times(ship.length)
    }

    Box(
        modifier = Modifier
            .offset {
                val cellSizePx = cellSize.toPx()
                IntOffset(
                    x = (ship.x * cellSizePx).roundToInt(),
                    y = (ship.y * cellSizePx).roundToInt()
                )
            }
            .size(width = width, height = height)
            .shadow(4.dp, RoundedCornerShape(4.dp))
            .background(FlotillaColors.Primary, RoundedCornerShape(4.dp))
            .border(2.dp, FlotillaColors.PrimaryDark, RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
    )
}

/**
 * Превью перетаскиваемого корабля
 */
@Composable
private fun DraggedShipPreview(
    ship: ShipTemplate,
    orientation: ShipPlacementValidator.Orientation,
    offset: Offset,
    cellSize: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val width = when (orientation) {
        ShipPlacementValidator.Orientation.HORIZONTAL -> with(density) { (cellSize * ship.length).toDp() }
        ShipPlacementValidator.Orientation.VERTICAL -> with(density) { cellSize.toDp() }
    }

    val height = when (orientation) {
        ShipPlacementValidator.Orientation.HORIZONTAL -> with(density) { cellSize.toDp() }
        ShipPlacementValidator.Orientation.VERTICAL -> with(density) { (cellSize * ship.length).toDp() }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = offset.x.roundToInt() - with(density) { (width.toPx() / 2).roundToInt() },
                    y = offset.y.roundToInt() - with(density) { (height.toPx() / 2).roundToInt() }
                )
            }
            .size(width = width, height = height)
            .shadow(8.dp, RoundedCornerShape(4.dp))
            .background(FlotillaColors.Primary.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .border(2.dp, FlotillaColors.PrimaryDark, RoundedCornerShape(4.dp))
    )
}
