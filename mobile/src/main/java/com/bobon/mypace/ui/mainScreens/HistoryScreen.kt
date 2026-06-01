package com.bobon.mypace.ui.mainScreens

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

/**
 * Модель данных для истории тренировок
 */
data class WorkoutHistoryItem(
    val id: Int,
    val date: String,
    val distance: String,
    val duration: String,
    val pace: String,
    val isRunning: Boolean
)

@Composable
fun HistoryScreen() {
    var selectedWorkout by remember { mutableStateOf<WorkoutHistoryItem?>(null) }

    if (selectedWorkout == null) {
        HistoryListContent(onWorkoutClick = { selectedWorkout = it })
    } else {
        // Вызываем экран деталей из отдельного файла
        WorkoutDetailScreen(
            workout = selectedWorkout!!,
            onBack = { selectedWorkout = null }
        )
    }
}

@Composable
fun HistoryListContent(onWorkoutClick: (WorkoutHistoryItem) -> Unit) {

    val historyList = remember {
        listOf(
            WorkoutHistoryItem(1, "Сегодня, 08:30", "5.20 км", "32:15", "6:12 мин/км", true),
            WorkoutHistoryItem(2, "Вчера, 18:45", "3.10 км", "45:20", "14:37 мин/км", false),
            WorkoutHistoryItem(3, "12 Окт, 07:10", "10.05 км", "58:40", "5:50 мин/км", true),
            WorkoutHistoryItem(4, "10 Окт, 19:20", "2.50 км", "35:00", "14:00 мин/км", false),
            WorkoutHistoryItem(5, "08 Окт, 09:00", "7.40 км", "48:12", "6:31 мин/км", true),
            WorkoutHistoryItem(6, "05 Окт, 08:00", "5.00 км", "31:00", "6:12 мин/км", true)
        )
    }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("Все", "Бег", "Ходьба")

    val filteredList = when (selectedFilterIndex) {
        1 -> historyList.filter { it.isRunning }
        2 -> historyList.filter { !it.isRunning }
        else -> historyList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "История", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TotalStatsHeader()

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filters.size) { index ->
                FilterChip(
                    selected = selectedFilterIndex == index,
                    onClick = { selectedFilterIndex = index },
                    label = { Text(filters[index]) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                WorkoutCard(item = item, onClick = { onWorkoutClick(item) })
            }
        }
    }
}

@Composable
fun TotalStatsHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Дистанция", "33.25", "км")
            StatItem("Занятий", "6", "")
            StatItem("Время", "4:10", "ч")
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(
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

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    MaterialTheme {
        HistoryScreen()
    }
}
