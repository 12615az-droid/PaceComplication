package com.example.pacecomplication.ui.mainScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacecomplication.R

/**
 * SettingsScreen — экран настроек приложения.
 *
 * Назначение:
 * - отображает настройки приложения, сгруппированные по разделам
 * - поддерживает вертикальный скролл и sticky-заголовки секций
 *
 * Текущее состояние:
 * - структура экрана и поведение скролла находятся в разработке
 * - элементы списка временные (заглушки)
 *
 * Особенности:
 * - используется собственный компактный заголовок вместо стандартного TopAppBar
 * - экран адаптирован под динамическую тему Material 3
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameяter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    Scaffold(
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CompactHeader(
                stringResource(R.string.settingHeader)
            )
        }
    )
    { padding ->
        // Временный список элементов.
// Используется только для проверки:
// - поведения скролла
// - stickyHeader
// - визуального восприятия экрана при большом количестве настроек
//
// Содержимое будет заменено на реальные настройки.
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {


        }

    }

}


/**
 * CompactHeader — компактный заголовок экрана настроек.
 *
 * Назначение:
 * - отображает название экрана
 * - занимает минимальную высоту
 * - учитывает системные отступы (status bar)
 *
 * Используется вместо стандартного TopAppBar,
 * так как экран настроек не требует расширенного AppBar.
 */
@Composable
fun CompactHeader(title: String) {
    Surface(color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Чуть увеличила, чтобы иконкам системы было не тесно
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall, // Сделаем чуть солиднее
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface // Контрастный текст к фону
            )
        }
    }
}


/**
 * SectionHeader — заголовок раздела в экране настроек.
 *
 * Назначение:
 * - визуально отделяет группы настроек друг от друга
 * - используется внутри LazyColumn
 * - может применяться как обычный элемент или stickyHeader
 *
 * Особенности оформления (Material 3):
 * - текст в верхнем регистре для "системного" вида
 * - увеличенный letterSpacing для читаемости
 * - используется акцентный цвет темы (primary)
 *
 * @param title название раздела настроек
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(), // В М3 заголовки секций часто делают капсом для строгости
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary, // Системный акцентный цвет
        letterSpacing = 1.sp
    )
}


/**
 * SettingsRow — универсальный элемент строки настроек.
 *
 * Назначение:
 * - отображает одну настройку в виде кликабельной карточки
 * - поддерживает иконку, заголовок и опциональный подзаголовок
 * - может отображать индикатор навигации (стрелку)
 *
 * Особенности дизайна:
 * - внешний вид адаптируется под светлую и тёмную тему
 * - в светлой теме используется белый фон (как в системных настройках)
 * - в тёмной теме используется surfaceContainer из Material 3
 *
 * @param title основной текст настройки
 * @param subtitle дополнительное описание (может отсутствовать)
 * @param icon иконка настройки
 * @param isNavigation показывает, ведёт ли строка на другой экран
 * @param onClick обработчик нажатия на строку
 */
@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isNavigation: Boolean = false,
    onClick: () -> Unit
) {
    // 1. Определяем цвета в зависимости от темы
    val isDark = isSystemInDarkTheme()

    val cardBackgroundColor = if (isDark) {
        // В темной теме берем системный темный оттенок
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        // В светлой теме — всегда белый, как ты и просил
        Color.White
    }

    val contentColor = if (isDark) {
        MaterialTheme.colorScheme.onSurface
    } else {
        // Для белой кнопки используем основной системный цвет (фиолетовый на твоем Пикселе)
        // или просто темный, чтобы было читаемо.
        MaterialTheme.colorScheme.primary
    }


    // Surface используется как кликабельная карточка настройки
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = cardBackgroundColor, // Тот самый белый фон
        shadowElevation = 2.dp // Небольшая тень, чтобы кнопки "парили" над фоном
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor // Иконка подстраивается под тему
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                    )
                }
            }

            // Стрелка показывается только если настройка ведёт на другой экран
            if (isNavigation) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    MaterialTheme {
        SettingsScreen()
    }
}


