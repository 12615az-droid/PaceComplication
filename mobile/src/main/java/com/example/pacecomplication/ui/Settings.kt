package com.example.pacecomplication.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import com.example.pacecomplication.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    Scaffold (containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0),
        topBar ={ CompactHeader(
            stringResource(R.string.settingHeader)
        )}
    )
    {
             padding ->
        // Временный список элементов.
// Используется только для проверки:
// - поведения скролла
// - stickyHeader
// - визуального восприятия экрана при большом количестве настроек
//
// Содержимое будет заменено на реальные настройки.
        LazyColumn (modifier = Modifier.padding(padding).padding(horizontal = 20.dp),){

            item { SectionHeader("Алгоритмы тренировки") }
            item {
                SettingsRow(
                    title = "Метод расчёта темпа",
                    subtitle = "Скользящее окно (30 сек)",
                    icon = Icons.Default.ShowChart,
                    isNavigation = false
                ) { /* Выбор метода */ }
            }
            item {
                SettingsRow(
                    title = "Фильтрация GPS",
                    subtitle = "Фильтр Калмана (высокая точность)",
                    icon = Icons.Default.MyLocation,
                    isNavigation = false
                ) { /* Настройка фильтра */ }
            }
            item {
                SettingsRow(
                    title = "Тип активности",
                    subtitle = "Бег по пересеченной местности",
                    icon = Icons.Default.DirectionsRun,
                    isNavigation = false
                ) { /* Диалог */ }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // --- СЕКЦИЯ 2: ОБОРУДОВАНИЕ ---
            item { SectionHeader("Датчики и железо") }
            item {
                SettingsRow(
                    title = "Внешний пульсометр",
                    subtitle = "Polar H10 (Подключено)",
                    icon = Icons.Default.BluetoothConnected,
                    isNavigation = true
                ) { /* Настройки BT */ }
            }
            item {
                SettingsRow(
                    title = "Калибровка шагомера",
                    subtitle = "Коэффициент 1.042",
                    icon = Icons.Default.Straighten,
                    isNavigation = false
                ) { /* Диалог ввода цифр */ }
            }
            item {
                SettingsRow(
                    title = "Частота опроса GPS",
                    subtitle = "1 Гц (Энергосбережение)",
                    icon = Icons.Default.BatteryChargingFull,
                    isNavigation = false
                ) { /* Выбор частоты */ }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // --- СЕКЦИЯ 3: АВТОМАТИЗАЦИЯ ---
            item { SectionHeader("Автоматизация") }
            item {
                SettingsRow(
                    title = "Автопауза",
                    subtitle = "При скорости ниже 2 км/ч",
                    icon = Icons.Default.PauseCircleFilled,
                    isNavigation = false
                ) { /* Настройка порога */ }
            }
            item {
                SettingsRow(
                    title = "Автоотсечка круга",
                    subtitle = "Каждые 1000 метров",
                    icon = Icons.Default.History,
                    isNavigation = false
                ) { /* Настройка круга */ }
            }
            item {
                SettingsRow(
                    title = "Таймер обратного отсчета",
                    subtitle = "10 секунд перед стартом",
                    icon = Icons.Default.Timer,
                    isNavigation = false
                ) { /* Настройка таймера */ }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // --- СЕКЦИЯ 4: ИНТЕРФЕЙС И ЭКРАН ---
            item { SectionHeader("Отображение") }
            item {
                SettingsRow(
                    title = "Сетка данных на экране",
                    subtitle = "3 поля (Темп, Дистанция, Пульс)",
                    icon = Icons.Default.GridView,
                    isNavigation = true
                ) { /* Конструктор экрана */ }
            }
            item {
                SettingsRow(
                    title = "Always on Display",
                    subtitle = "Не гасить экран во время бега",
                    icon = Icons.Default.StayCurrentPortrait,
                    isNavigation = false
                ) { /* Переключатель в диалоге */ }
            }
            item {
                SettingsRow(
                    title = "Цветовая схема",
                    subtitle = "Высококонтрастная (Ночь)",
                    icon = Icons.Default.Contrast,
                    isNavigation = false
                ) { /* Выбор темы */ }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // --- СЕКЦИЯ 5: СИСТЕМА И ХРАНИЛИЩЕ ---
            item { SectionHeader("Данные") }
            item {
                SettingsRow(
                    title = "Экспорт в GPX",
                    subtitle = "Автоматически после финиша",
                    icon = Icons.Default.Share,
                    isNavigation = false
                ) { /* Настройки экспорта */ }
            }
            item {
                SettingsRow(
                    title = "Локальное хранилище",
                    subtitle = "Занято 128 МБ из 2 ГБ",
                    icon = Icons.Default.Storage,
                    isNavigation = true
                ) { /* Очистка кеша */ }
            }
            item {
                SettingsRow(
                    title = "Версия прошивки алгоритма",
                    subtitle = "v2.4.1-build-89",
                    icon = Icons.Default.Info,
                    isNavigation = true
                ) { /* О приложении */ }
            }
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


