package com.bobon.mypace.ui.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.dp
import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.ui.history.HistoryViewModel
import com.bobon.mypace.ui.model.WorkoutHistoryItem
import com.bobon.mypace.utils.PaceFormatter
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.tooling.preview.Preview

import com.bobon.mypace.domain.model.TotalStats

/**
 * Модель данных для истории тренировок
 */




@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    var selectedWorkout by remember { mutableStateOf<WorkoutHistoryItem?>(null) }

    // Подписываемся на данные из ViewModel (теперь это List<Workout>)
    val historyList by viewModel.historyList.collectAsState()
    val totalStats by viewModel.totalStats.collectAsState()
    val selectedFilterIndex by viewModel.selectedFilter.collectAsState()

    if (selectedWorkout == null) {
        HistoryListContent(
            historyList = historyList,
            totalStats = totalStats ,
            selectedFilterIndex = selectedFilterIndex,
            onFilterSelect = { viewModel.selectFilter(it) },
            onWorkoutClick = { selectedWorkout = it },
            deleteTrack = { viewModel.deleteTrack(it) }
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
    totalStats: TotalStats,
    selectedFilterIndex: Int,
    onFilterSelect: (Int) -> Unit,
    onWorkoutClick: (WorkoutHistoryItem) -> Unit,
    deleteTrack: (String) -> Unit
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
                    WorkoutCard(
                        item = item,
                        onClick = { onWorkoutClick(item) },
                        onLongPress = { deleteTrack(item.id) }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TotalStatsHeader(totalStats: TotalStats){
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
fun WorkoutCard(
    item: WorkoutHistoryItem,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            )
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
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить тренировку?") },
            text = { Text("${item.date} • ${item.distance}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLongPress()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}




@Preview(
    name = "History Screen - With workouts",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HistoryListContentPreview() {
    MaterialTheme {
        HistoryListContent(
            historyList = listOf(
                WorkoutHistoryItem(
                    id = "1",
                    date = "16 июня 2026",
                    distance = "5.24 км",
                    duration = "00:28:14",
                    pace = "5:23 мин/км",
                    isRunning = true
                ),
                WorkoutHistoryItem(
                    id = "2",
                    date = "15 июня 2026",
                    distance = "3.10 км",
                    duration = "00:35:42",
                    pace = "11:31 мин/км",
                    isRunning = false
                ),
                WorkoutHistoryItem(
                    id = "3",
                    date = "13 июня 2026",
                    distance = "10.00 км",
                    duration = "00:57:08",
                    pace = "5:43 мин/км",
                    isRunning = true
                )
            ),
            totalStats = TotalStats(
                distanceKm = 18.34,
                workoutCount = 3,
                totalHours = 2.01
            ),
            selectedFilterIndex = 0,
            onFilterSelect = {},
            onWorkoutClick = {},
            deleteTrack = {}
        )
    }
}






