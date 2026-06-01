package com.bobon.mypace.ui.mainScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bobon.mypace.database.WorkoutEntity
import com.bobon.mypace.ui.TrainingViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Модель данных для истории тренировок
 */
data class WorkoutHistoryItem(
    val id: String, // Изменено на String, так как в БД ID может быть строкой
    val date: String,
    val distance: String,
    val duration: String,
    val pace: String,
    val isRunning: Boolean
)

// Утилиты для форматирования данных из БД в красивый текст
@SuppressLint("DefaultLocale")
fun formatDuration(startTime: Long, endTime: Long): String {
    val durationMs = endTime - startTime
    if (durationMs <= 0) return "00:00"
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

@SuppressLint("DefaultLocale")
fun calculatePace(startTime: Long, endTime: Long, distanceMeters: Double): String {
    if (distanceMeters <= 0) return "0:00 мин/км"
    val durationMinutes = (endTime - startTime) / 60000.0
    val distanceKm = distanceMeters / 1000.0
    val paceMinPerKm = durationMinutes / distanceKm

    val minutes = paceMinPerKm.toInt()
    val seconds = ((paceMinPerKm - minutes) * 60).toInt()
    return String.format("%d:%02d мин/км", minutes, seconds)
}

// Extension-функция для маппинга сущности БД в модель UI
@SuppressLint("DefaultLocale")
fun WorkoutEntity.toUiModel(): WorkoutHistoryItem {
    return WorkoutHistoryItem(
        id = this.id,
        date = formatDate(this.startTime),
        distance = String.format("%.2f км", this.totalDistance / 1000.0),
        duration = formatDuration(this.startTime, this.endTime),
        pace = calculatePace(this.startTime, this.endTime, this.totalDistance),
        isRunning = this.activityType == 1 // Предполагаем, что 1 - это Бег, 2 - Ходьба (как в ViewModel)
    )
}

@Composable
fun HistoryScreen(viewModel: TrainingViewModel = koinViewModel()) {
    var selectedWorkout by remember { mutableStateOf<WorkoutHistoryItem?>(null) }

    // Подписываемся на данные из ViewModel
    val workoutsEntities by viewModel.workouts.collectAsState()
    val totalStats by viewModel.totalStats.collectAsState()
    val selectedFilterIndex by viewModel.selectedFilter.collectAsState()

    // Маппим полученные сущности из БД в UI модели
    val historyList = remember(workoutsEntities) {
        workoutsEntities.map { it.toUiModel() }
    }

    if (selectedWorkout == null) {
        HistoryListContent(
            historyList = historyList,
            totalStats = totalStats,
            selectedFilterIndex = selectedFilterIndex,
            onFilterSelect = { viewModel.selectFilter(it) },
            onWorkoutClick = { selectedWorkout = it }
        )
    } else {
        WorkoutDetailScreen(
            workout = selectedWorkout!!,
            onBack = { selectedWorkout = null }
        )
    }
}

@Composable
fun HistoryListContent(
    historyList: List<WorkoutHistoryItem>,
    totalStats: TrainingViewModel.TotalStats,
    selectedFilterIndex: Int,
    onFilterSelect: (Int) -> Unit,
    onWorkoutClick: (WorkoutHistoryItem) -> Unit
) {
    val filters = listOf("Все", "Бег", "Ходьба")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "История", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Передаем статистику из БД
        TotalStatsHeader(totalStats)

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filters.size) { index ->
                FilterChip(
                    selected = selectedFilterIndex == index,
                    onClick = { onFilterSelect(index) },
                    label = { Text(filters[index]) }
                )
            }
        }

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Список тренировок пуст", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    WorkoutCard(item = item, onClick = { onWorkoutClick(item) })
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TotalStatsHeader(totalStats: TrainingViewModel.TotalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Дистанция", String.format("%.2f", totalStats.distanceKm), "км")
            StatItem("Занятий", totalStats.workoutCount.toString(), "")

            // Конвертация часов в часы и минуты
            val hours = totalStats.totalHours.toInt()
            val minutes = ((totalStats.totalHours - hours) * 60).toInt()
            StatItem("Время", String.format("%d:%02d", hours, minutes), "ч")
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) Text(" $unit", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun WorkoutCard(item: WorkoutHistoryItem, onClick: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (item.isRunning) Color(0xFFFF9800).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isRunning) Icons.AutoMirrored.Filled.DirectionsRun else Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = if (item.isRunning) Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text(text = item.distance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = item.duration, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = item.pace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}