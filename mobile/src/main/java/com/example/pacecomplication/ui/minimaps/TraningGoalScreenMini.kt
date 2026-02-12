package com.example.pacecomplication.ui.minimaps


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * GoalsDialog — компактное окно выбора целей (не на весь экран).
 *
 * Идея:
 * - список целей (темп/пульс/время/дистанция)
 * - каждая цель выключена по умолчанию
 * - ползунки/настройки появляются только когда цель включили
 *
 * Пока чисто дизайн/черновик. Данные наружу не отдаём.
 */
@Composable
fun GoalsDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    if (!open) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GoalsDialogContent(
                onDismiss = onDismiss,
                onApply = onApply
            )
        }
    }
}

@Composable
private fun GoalsDialogContent(
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    // Включатели целей
    var paceEnabled by remember { mutableStateOf(false) }
    var hrEnabled by remember { mutableStateOf(false) }
    var timeEnabled by remember { mutableStateOf(false) }
    var distEnabled by remember { mutableStateOf(false) }

    // Значения (пока заглушки для дизайна)
    var paceMin by remember { mutableFloatStateOf(5.0f) }    // мин/км
    var paceMax by remember { mutableFloatStateOf(6.0f) }    // мин/км
    var hrTarget by remember { mutableFloatStateOf(150f) }   // bpm
    var timeTarget by remember { mutableFloatStateOf(30f) }  // minutes
    var distTarget by remember { mutableFloatStateOf(5f) }   // km

    val canApply = paceEnabled || hrEnabled || timeEnabled || distEnabled

    Column(modifier = Modifier.padding(16.dp)) {

        Header(onDismiss = onDismiss)

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        GoalToggleItem(
            title = "Темп",
            subtitle = if (paceEnabled)
                "Диапазон: ${"%.2f".format(paceMin)} – ${"%.2f".format(paceMax)} мин/км"
            else
                "Выключено",
            icon = Icons.Outlined.Speed,
            checked = paceEnabled,
            onCheckedChange = { paceEnabled = it }
        ) {
            RangeSlider(
                value = paceMin..paceMax,
                onValueChange = {
                    paceMin = it.start
                    paceMax = it.endInclusive
                },
                valueRange = 3f..10f
            )
        }

        Spacer(Modifier.height(10.dp))

        GoalToggleItem(
            title = "Пульс",
            subtitle = if (hrEnabled) "Цель: ${hrTarget.toInt()} bpm" else "Выключено",
            icon = Icons.Outlined.FavoriteBorder,
            checked = hrEnabled,
            onCheckedChange = { hrEnabled = it }
        ) {
            Slider(
                value = hrTarget,
                onValueChange = { hrTarget = it },
                valueRange = 90f..200f
            )
        }

        Spacer(Modifier.height(10.dp))

        GoalToggleItem(
            title = "Время",
            subtitle = if (timeEnabled) "Цель: ${timeTarget.toInt()} мин" else "Выключено",
            icon = Icons.Outlined.Timer,
            checked = timeEnabled,
            onCheckedChange = { timeEnabled = it }
        ) {
            Slider(
                value = timeTarget,
                onValueChange = { timeTarget = it },
                valueRange = 5f..180f
            )
        }

        Spacer(Modifier.height(10.dp))

        GoalToggleItem(
            title = "Дистанция",
            subtitle = if (distEnabled) "Цель: ${"%.1f".format(distTarget)} км" else "Выключено",
            icon = Icons.Outlined.Route,
            checked = distEnabled,
            onCheckedChange = { distEnabled = it }
        ) {
            Slider(
                value = distTarget,
                onValueChange = { distTarget = it },
                valueRange = 1f..50f
            )
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        Actions(
            canApply = canApply,
            onCancel = onDismiss,
            onApply = onApply
        )
    }
}

@Composable
private fun Header(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Цели тренировки",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun Actions(
    canApply: Boolean,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onCancel) { Text("Отмена") }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onApply,
            enabled = canApply
        ) { Text("Применить") }
    }
}

/**
 * GoalToggleItem — карточка-пункт цели:
 * - заголовок + подзаголовок
 * - switch справа
 * - при включении раскрывает content (ползунки)
 */
@Composable
private fun GoalToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            AnimatedVisibility(visible = checked) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Goals dialog (content preview)")
@Composable
private fun GoalsDialogPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        ) {
            GoalsDialogContent(
                onDismiss = {},
                onApply = {}
            )
        }
    }
}