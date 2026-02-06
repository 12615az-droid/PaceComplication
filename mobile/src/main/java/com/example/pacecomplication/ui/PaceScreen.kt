package com.example.pacecomplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pacecomplication.modes.TrainingMode
import com.example.pacecomplication.modes.WalkingMode
import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.WorkoutState



/**
 * Описание вкладок нижней навигации (Bottom Navigation).
 *
 * Используется как единый источник правды для:
 * - списка вкладок в нижнем меню
 * - маршрутов (route) для NavHost
 *
 * Поля:
 * @param route строковый ключ маршрута для навигации
 * @param title подпись для NavigationBarItem
 * @param icon  иконка для NavigationBarItem
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Training : Screen("training", "Тренировка", Icons.Default.PlayArrow)
    object History : Screen("history", "История", Icons.Default.List)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}

/* Список вкладок, которые показываем в NavigationBar (внизу) */
private val bottomItems = listOf(Screen.Training, Screen.History, Screen.Settings)

data class TrainingUiState(
    val pace: String,
    val accuracy: Float,
    val timeMs: Long,
    val mode: TrainingMode,
    val workoutState: WorkoutState,
) {
    val isWalking: Boolean get() = mode == WalkingMode
    val isModeChangeLocked: Boolean get() = workoutState == WorkoutState.ACTIVE
    val isSaveEnabled: Boolean get() = workoutState == WorkoutState.ACTIVE && timeMs > 0
}

data class TrainingActions(
    val onStart: () -> Unit,
    val onStop: () -> Unit,
    val onSave: () -> Unit,
    val onToggleMode: () -> Unit, // оставляем твой toggle, раз тебе так удобно
)


/**
 * Экран PaceScreen — связующее звено между логикой и UI.
 *
 * Назначение:
 * - Подписывается на состояния из LocationRepository
 *   (темп, точность GPS, текущий режим активности).
 * - Преобразует бизнес-состояние в простой UI-формат.
 * - Передаёт данные и события в TrainingScreen.
 *
 * Что делает внутри:
 * - collectAsState() — получает актуальные значения из Flow
 * - преобразует ActivityMode в флаг isWalking, понятный UI
 * - не содержит визуальной разметки
 *
 * Что НЕ делает:
 * - не рисует UI
 * - не управляет навигацией
 * - не содержит бизнес-логики трекинга
 *
 * Параметры:
 * @param onStartClick  вызывается при нажатии кнопки "Старт"
 * @param onStopClick   вызывается при нажатии кнопки "Стоп"
 * @param onSaveClick   вызывается при сохранении тренировки
 * @param onModeChanged вызывается при переключении режима (бег / ходьба)
 *
 * Зачем так:
 * - UI (TrainingScreen) остаётся чистым и переиспользуемым
 * - логика получения данных изолирована в одном месте
 * - упрощается тестирование и поддержка
 */
@Composable
fun PaceScreen(
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onSaveClick: () -> Unit,
    onModeChanged: () -> Unit
) {

    val pace by LocationRepository.currentPace.collectAsState(initial = "0:00")
    val accuracy by LocationRepository.currentGPSAccuracy.collectAsState(initial = 0f)
    val timeMs by LocationRepository.trainingTimeMs.collectAsState(initial = 0L)
    val mode by LocationRepository.activityMode.collectAsState()
    val workoutState by LocationRepository.workoutState.collectAsState()



    val state = TrainingUiState(
        pace = pace,
        accuracy = accuracy,
        timeMs = timeMs,
        mode = mode,
        workoutState = workoutState
    )

    val actions = TrainingActions(
        onStart = onStartClick,
        onStop = onStopClick,
        onSave = onSaveClick,
        onToggleMode = onModeChanged
    )



    PaceAppShell(
        state,
        actions
    )
}


/**
 * PaceAppShell — основной UI-каркас приложения.
 *
 * Назначение:
 * - создаёт и хранит NavController
 * - задаёт Scaffold (контейнер всего экрана)
 * - отображает нижнюю панель навигации
 * - переключает экраны через NavHost
 *
 * Ответственность:
 * - управление структурой экранов
 * - навигация между вкладками (Training / History / Settings)
 *
 * Важно:
 * - бизнес-логики здесь нет
 * - данные и события приходят извне
 * - визуальные экраны подключаются через NavHost
 *
 * Параметры:
 * @param pace        текущий темп для экрана тренировки
 * @param accuracy    точность GPS (метры)
 * @param isWalking   флаг режима активности (по текущему неймингу проекта)
 * @param onStartClick  обработчик кнопки "Старт"
 * @param onStopClick   обработчик кнопки "Стоп"
 * @param onSaveClick   обработчик кнопки "Сохранить"
 * @param onModeChanged обработчик смены режима (бег / ходьба)
 */
@Composable
fun PaceAppShell(
    state: TrainingUiState, actions: TrainingActions) {
    // Контроллер навигации между экранами нижнего меню
    val navController = rememberNavController()

    // Scaffold — корневой контейнер экрана
    // Содержит нижнюю навигацию и область для экранов
    Scaffold(
        // Нижняя панель навигации (переключение вкладок)
        bottomBar = {
            BottomBar(
                navController = navController,
                items = bottomItems
            )
        }
    ) {
        // innerPadding учитывает высоту нижней навигации
        // и предотвращает перекрытие контента
            innerPadding ->

        // NavHost — точка переключения экранов приложения
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            state =state,
            actions = actions
        )
    }
}


/**
 * BottomBar — нижняя панель навигации приложения.
 *
 * Назначение:
 * - отображает вкладки основного меню (Training / History / Settings)
 * - показывает текущую активную вкладку
 * - переключает экраны через NavController
 *
 * Как работает:
 * - текущее состояние навигации берётся из navController.backStack
 * - активная вкладка определяется по совпадению route
 * - при нажатии выполняется navigate() с контролем back stack
 *
 * Особенности навигации:
 * - popUpTo(startDestination) — не накапливаем стек вкладок
 * - saveState / restoreState — сохраняем состояние экранов
 * - launchSingleTop — избегаем повторного создания экрана
 *
 * @param navController контроллер навигации приложения
 * @param items список вкладок нижнего меню (Screen)
 */
@Composable
fun BottomBar(
    navController: androidx.navigation.NavHostController,
    items: List<Screen>
) {
    // NavigationBar — контейнер нижнего меню (Material 3)
    NavigationBar {

        // Текущая запись стека навигации
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        // Текущий destination (нужен для подсветки активной вкладки)
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            // Определяем, активна ли вкладка
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    // Навигация между вкладками
                    navController.navigate(screen.route) {

                        // Очищаем стек до стартового экрана
                        // (чтобы не копить экраны при переключении вкладок)
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }

                        // Не создаём новый экран, если он уже открыт
                        launchSingleTop = true

                        // Восстанавливаем сохранённое состояние вкладки
                        restoreState = true
                    }
                }
            )
        }
    }
}


/**
 * AppNavHost — навигационный хост приложения.
 *
 * Назначение:
 * - описывает маршруты (routes) экранов приложения
 * - связывает Screen.route с соответствующими composable-экранами
 * - задаёт стартовый экран приложения
 *
 * Как используется:
 * - вызывается из PaceAppShell внутри Scaffold
 * - отображает нужный экран в зависимости от текущего route
 *
 * Особенности:
 * - TrainingScreen получает все необходимые данные извне
 * - HistoryScreen и SettingsScreen пока являются заглушками
 * - навигация управляется исключительно через NavController
 *
 * Параметры:
 * @param navController контроллер навигации приложения
 * @param modifier модификатор контейнера NavHost (обычно innerPadding от Scaffold)
 * @param pace        текущий темп для экрана тренировки
 * @param accuracy    точность GPS (метры)
 * @param isWalking   флаг режима активности для TrainingScreen
 * @param onStartClick  обработчик кнопки "Старт"
 * @param onStopClick   обработчик кнопки "Стоп"
 * @param onSaveClick   обработчик кнопки "Сохранить"
 * @param onModeChanged обработчик смены режима (бег / ходьба)
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    state: TrainingUiState,
    actions: TrainingActions,
) {
    // NavHost — контейнер, который отображает экран согласно текущему route
    NavHost(
        navController = navController,
        // Экран, который открывается при старте приложения
        startDestination = Screen.Training.route,
        modifier = modifier
    ) {
        composable(Screen.Training.route) {
            // Главный экран тренировки
            TrainingScreen(
                state = state,
                actions = actions
            )
        }

        composable(Screen.History.route) {
            // Экран истории тренировок (пока заглушка)
            HistoryScreen()
        }

        composable(Screen.Settings.route) {
            // Экран настроек приложения
            SettingsScreen()
        }
    }
}


@Composable
fun HistoryScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Тут будет история и графики")
    }
}


