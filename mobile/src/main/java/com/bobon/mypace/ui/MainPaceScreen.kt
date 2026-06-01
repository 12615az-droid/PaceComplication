package com.bobon.mypace.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bobon.mypace.WorkoutState
import com.bobon.mypace.ui.mainScreens.HistoryScreen
import com.bobon.mypace.ui.mainScreens.SettingsScreen
import com.bobon.mypace.ui.mainScreens.TrainingScreenRoute
import com.bobon.mypace.ui.mainScreens.TrainingSetupScreenRoute
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

/**
 * Описание вкладок нижней навигации (Bottom Navigation).
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Training : Screen("training", "Тренировка", Icons.Default.PlayArrow)
    object History : Screen("history", "История", Icons.Default.List)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}

/* Список вкладок, которые показываем в NavigationBar (внизу) */
private val bottomItems = listOf(Screen.Training, Screen.History, Screen.Settings)

@Composable
fun PaceScreenRoute(
    viewModel: TrainingViewModel = koinViewModel()
) {
    val workoutState by viewModel.workoutState.collectAsState()
    PaceScreen(workoutState = workoutState, onPermissionGrantedStart = viewModel::startTracking)
}

@Composable
fun PaceScreen(
    workoutState: WorkoutState,
    onPermissionGrantedStart: () -> Unit
) {
    if (workoutState == WorkoutState.IDLE) {
        PaceAppShellRoute()
    } else {
        TrainingScreenRoute()
    }
}

@Composable
fun PaceAppShellRoute(
    viewModel: TrainingViewModel = koinViewModel()
) {
    PaceAppShell(onScreenChanged = viewModel::logScreenChanged)
}

@Composable
fun PaceAppShell(
    onScreenChanged: (String) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController, items = bottomItems
            )
        }) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onScreenChanged = onScreenChanged
        )
    }
}

@Composable
fun BottomBar(
    navController: NavHostController, items: List<Screen>
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onScreenChanged: (String) -> Unit
) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .map { it.destination.route }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { route ->
                onScreenChanged(route)
            }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Training.route, modifier = modifier
    ) {
        composable(Screen.Training.route) {
            TrainingSetupScreenRoute()
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaceScreenPreview() {
    PaceScreen(
        workoutState = WorkoutState.IDLE,
        onPermissionGrantedStart = {}
    )
}
