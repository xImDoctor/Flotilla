package com.imdoctor.flotilla.presentation.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdoctor.flotilla.presentation.screens.game.models.Board
import com.imdoctor.flotilla.presentation.screens.game.models.Cell
import com.imdoctor.flotilla.presentation.screens.game.models.CellState

/**
 * Игровое поле 10x10
 *
 * @param board Данные игрового поля
 * @param isInteractive Можно ли нажимать на клетки (true для поля противника)
 * @param onCellClick Callback при клике на клетку
 * @param modifier Модификатор
 */
@Composable
fun GameBoardGrid(
    board: Board,
    isInteractive: Boolean = false,
    onCellClick: (x: Int, y: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Заголовок с буквами (A-J)
            Row(modifier = Modifier.fillMaxWidth()) {
                // Пустая клетка в углу
                Box(modifier = Modifier.size(24.dp))

                // Буквы столбцов
                for (x in 0..9) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('A' + x).toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Строки с номерами и клетками
            for (y in 0..9) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Номер строки
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (y + 1).toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Клетки
                    for (x in 0..9) {
                        val cell = board.getCell(x, y) ?: Cell(x, y, CellState.EMPTY)
                        CellItem(
                            cell = cell,
                            isInteractive = isInteractive,
                            onClick = { onCellClick(x, y) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Отдельная клетка игрового поля
 *
 * @param cell Данные клетки
 * @param isInteractive Можно ли нажать на клетку
 * @param onClick Callback при клике
 */
@Composable
fun CellItem(
    cell: Cell,
    isInteractive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = when (cell.state) {
        CellState.EMPTY -> Color.Transparent
        CellState.SHIP -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        CellState.HIT -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        CellState.MISS -> MaterialTheme.colorScheme.surfaceVariant
        CellState.SUNK -> MaterialTheme.colorScheme.error
    }

    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .padding(1.dp)
            .background(backgroundColor, shape = RoundedCornerShape(2.dp))
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .then(
                if (isInteractive && (cell.state == CellState.EMPTY || cell.state == CellState.SHIP)) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Иконка для разных состояний
        when (cell.state) {
            CellState.HIT, CellState.SUNK -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hit",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
            CellState.MISS -> {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
            else -> {
                // Пустая клетка или корабль без иконки
            }
        }
    }
}

/**
 * Компактная информация об игре (никнейм противника, чей ход)
 *
 * @param opponentNickname Никнейм противника
 * @param isYourTurn Ваш ли сейчас ход
 */
@Composable
fun GameInfo(
    opponentNickname: String,
    isYourTurn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isYourTurn) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Противник: $opponentNickname",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isYourTurn) "Ваш ход" else "Ход противника",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isYourTurn) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Диалог победы
 */
@Composable
fun VictoryDialog(
    totalMoves: Int,
    durationSeconds: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎉 Победа!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Поздравляем! Вы победили!")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Всего ходов: $totalMoves")
                Text("Время игры: ${formatDuration(durationSeconds)}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Диалог поражения
 */
@Composable
fun DefeatDialog(
    totalMoves: Int,
    durationSeconds: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Поражение",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("К сожалению, вы проиграли.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Всего ходов: $totalMoves")
                Text("Время игры: ${formatDuration(durationSeconds)}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Форматировать длительность в минуты:секунды
 */
private fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return String.format("%d:%02d", minutes, secs)
}
