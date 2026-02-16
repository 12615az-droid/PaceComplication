package com.example.pacecomplication.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pacecomplication.WorkoutState
import com.example.pacecomplication.modes.WalkingMode
import com.example.pacecomplication.ui.goals.GoalsDialog
import org.koin.androidx.compose.koinViewModel

/**
 * TrainingSetupScreen — экран подготовки перед стартом тренировки (IDLE).
 *
 * Тут:
 * - выбор режима (Бег/Ходьба)
 * - короткая подсказка
 * - большая кнопка "Старт"
 *
 * Логики репозитория тут НЕТ — только UI + колбэки.
 */
@Composable
fun TrainingSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingViewModel = koinViewModel()
) {
    val mode by viewModel.activityMode.collectAsState()
    val isGoalSetupOpen by viewModel.isGoalSetupOpen.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            GoalsDialog(isGoalSetupOpen, { viewModel.onCloseGoalSetup() }, {})

            SetupHeader()


            Spacer(Modifier.height(24.dp))

            ModeSelectionCard(
                isWalking = mode == WalkingMode,
                onModeToggle = { viewModel.onModeChanged() }
            )

            Spacer(Modifier.height(16.dp))

            GoalsSelectionCard(
                onClick = { viewModel.onOpenGoalSetup() }
            )

            Spacer(Modifier.height(16.dp))

            QuickSettingsPlaceholder()

            Spacer(Modifier.weight(1f))

            StartWorkoutButton(
                onClick = { viewModel.startTracking() }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SetupHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Подготовка тренировки",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Выбери режим и параметры перед стартом.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSelectionCard(
    isWalking: Boolean,
    onModeToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Режим",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            ModeSelector(
                isWalking = isWalking,
                onModeChanged = onModeToggle,
                isModeChangeLocked = false
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (isWalking)
                    "Спокойный темп, сильнее сглаживание GPS."
                else
                    "Бег, темп более «живой».",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(
    isWalking: Boolean,
    onModeChanged: () -> Unit,
    isModeChangeLocked: Boolean,
) {
    val options = listOf("БЕГ", "ХОДЬБА")

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { index, label ->

            // Выбрана ли именно эта кнопка:
            // index 0 => "БЕГ"  (isWalking = false)
            // index 1 => "ХОДЬБА" (isWalking = true)
            val isThisSelected = (index == 1 && isWalking) || (index == 0 && !isWalking)

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    // Важно: клики по уже выбранной кнопке игнорируем
                    if (!isThisSelected) onModeChanged()
                },
                selected = isThisSelected,
                label = { Text(label, fontWeight = FontWeight.Bold) },
                enabled = !isModeChangeLocked
            )
        }
    }
}


@Composable
private fun GoalsSelectionCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Выберите цели",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Дистанция, время, калории, круги…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "—",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickSettingsPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Быстрые настройки",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Автопауза, экран не гасить и т.д.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StartWorkoutButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = "Старт",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
    }
}





