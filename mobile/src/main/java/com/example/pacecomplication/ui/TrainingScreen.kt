package com.example.pacecomplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.R
import com.example.pacecomplication.WorkoutState
import com.example.pacecomplication.WorkoutTimer
import com.example.pacecomplication.modes.WalkingMode


/**
 * TrainingScreen — основной UI-экран тренировки.
 *
 * Назначение:
 * - отображает текущий темп и точность GPS
 * - позволяет переключать режим (бег / ходьба)
 * - содержит кнопки управления трекингом
 *
 * Особенности:
 * - экран stateless: не хранит бизнес-состояние
 * - получает все данные и события через параметры
 * - не знает про Repository и Service
 *
 * Архитектурный принцип:
 * - состояние собирается выше (PaceScreen)
 * - TrainingScreen отвечает только за отображение
 **/

/**
 * ModeSelector — переключатель режима активности.
 *
 * Назначение:
 * - позволяет выбрать режим тренировки (БЕГ / ХОДЬБА)
 *
 * Особенности реализации:
 * - использует SegmentedButton (Material 3)
 * - при нажатии на уже выбранный режим событие не отправляется
 *
 * @param isWalking текущий режим (true — ходьба, false — бег)
 * @param onModeChanged событие переключения режима
 */
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

/**
 * SignalStatus — UI-модель состояния GPS-сигнала.
 *
 * Используется для:
 * - отображения текстового статуса
 * - выбора цвета темпа и бейджа
 *
 * @param text текстовое описание качества сигнала
 * @param color цвет, соответствующий уровню сигнала
 */

data class SignalStatus(val text: String, val color: Color)


/**
 * Определяет UI-статус GPS-сигнала на основе точности.
 *
 * Логика:
 * - accuracy <= 0   — сигнала нет
 * - accuracy <= 8   — отличный сигнал
 * - accuracy <= 20  — средний сигнал
 * - accuracy > 20   — слабый сигнал
 *
 * Возвращает объект SignalStatus для использования в UI.
 *
 * @param accuracy точность GPS в метрах
 */

@Composable
fun getSignalStatus(accuracy: Float): SignalStatus {
    return when {
        accuracy <= 0f -> SignalStatus("", Color.Gray)
        accuracy <= 8f -> SignalStatus(stringResource(R.string.status_perfect), Color.Green)
        accuracy <= 20f -> SignalStatus(
            stringResource(R.string.status_average),
            Color(0xFFFFC107)
        ) // желтый
        else -> SignalStatus(stringResource(R.string.status_weak), Color.Red)
    }
}


/**
 * TrainingScreen — основной экран тренировки.
 *
 * Назначение:
 * - отображает темп, точность GPS и статус сигнала
 * - позволяет управлять трекингом (старт / стоп / сохранить)
 * - отображает текущий режим активности
 *
 * Принцип работы:
 * - не хранит бизнес-состояние
 * - получает данные и события через параметры
 * - реагирует на изменения состояния автоматически
 *
 * @param pace текущий темп (мм:сс)
 * @param accuracy точность GPS в метрах
 * @param isWalking флаг режима активности
 * @param onModeChanged обработчик смены режима
 * @param onStartClick обработчик кнопки "Старт"
 * @param onStopClick обработчик кнопки "Стоп"
 * @param onSaveClick обработчик кнопки "Сохранить"
 */

@Composable
fun TrainingScreen(
    state: TrainingUiState,
    actions: TrainingActions,
) {
    // Определяем визуальный статус GPS-сигнала (цвет + текст)
    val status = getSignalStatus(state.accuracy)
    val workTime = WorkoutTimer()
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Переключение режима (БЕГ/ХОДЬБА)
            ModeSelector(
                isWalking = state.isWalking,
                onModeChanged = actions.onToggleMode,
                isModeChangeLocked = state.isModeChangeLocked
            )

            Spacer(Modifier.height(24.dp))

            WorkoutStatsBlock(workTime.formatTimer(state.timeMs))

            Spacer(Modifier.height(24.dp))

            // Темп + точность + бейдж (визуальный статус сигнала)
            PaceStatusBlock(
                pace = state.pace,
                accuracy = state.accuracy,
                status = status
            )

            Spacer(Modifier.height(24.dp))

            // Кнопки управления трекингом
            ControlButtons(
                isTracking = (state.workoutState == WorkoutState.ACTIVE)
                        && LocationRepository.isTracking.collectAsState().value,
                onStartClick = actions.onStart,
                onStopClick = actions.onStop,
                onSaveClick = actions.onSave,
                isSaveEnabled = state.isSaveEnabled
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * PaceStatusBlock — UI-блок отображения темпа и GPS-статуса.
 *
 * Содержит:
 * - крупное значение темпа
 * - информацию о точности GPS
 * - цветовой бейдж качества сигнала
 *
 * @param pace текущий темп
 * @param accuracy точность GPS
 * @param status визуальный статус сигнала
 */
@Composable
fun PaceStatusBlock(
    pace: String,
    accuracy: Float,
    status: SignalStatus
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Главный крупный текст темпа
        Text(
            text = pace,
            fontSize = 100.sp,
            fontWeight = FontWeight.Black,
            color = status.color
        )

        // Точность: если нет сигнала -> "готовность", иначе показываем число
        if (accuracy > 0f) {
            Text(
                text = stringResource(id = R.string.Accuracy, accuracy),
                fontSize = 18.sp,
                color = status.color
            )
        } else {
            Text(
                text = stringResource(id = R.string.status_ready, accuracy),
                fontSize = 18.sp,
                color = status.color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Бейдж со статусом (если текст пустой — не рисуем)
        if (status.text.isNotEmpty()) {
            Surface(
                color = status.color.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = status.text,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    color = status.color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun WorkoutStatsBlock(
    time: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = time,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/**
 * ControlButtons — набор кнопок управления трекингом.
 *
 * Кнопки:
 * - Старт — доступна, когда трекинг не запущен
 * - Стоп — доступна во время трекинга
 * - Сохранить — доступна во время трекинга
 *
 * @param isTracking текущее состояние трекинга
 * @param onStartClick событие "Старт"
 * @param onStopClick событие "Стоп"
 * @param onSaveClick событие "Сохранить"
 */
@Composable
fun ControlButtons(
    isTracking: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStartClick,
            enabled = !isTracking,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .weight(1f)
                .widthIn(min = 110.dp)
                .height(64.dp)
        ) {
            Text(stringResource(id = R.string.StartButton), maxLines = 1, softWrap = false)
        }

        Button(
            onClick = onStopClick,
            enabled = isTracking,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .weight(1f)
                .widthIn(min = 110.dp)
                .height(64.dp)
        ) {
            Text(stringResource(id = R.string.StopButton), maxLines = 1, softWrap = false)
        }

        Button(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .weight(1f)
                .widthIn(min = 110.dp)
                .height(64.dp)
        ) {
            Text(stringResource(id = R.string.SaveButton), maxLines = 1, softWrap = false)
        }
    }
}

@Preview(showBackground = true, name = "Идеальный сигнал")
@Composable
fun PreviewTrainingGood() {
    MaterialTheme {
        TrainingScreen(
            state = TrainingUiState(
                pace = "5:40",
                accuracy = 3f,
                timeMs = 1751111,
                mode = WalkingMode,
                workoutState = WorkoutState.ACTIVE
            ), TrainingActions({}, {}, {}, {})
        )
    }
}