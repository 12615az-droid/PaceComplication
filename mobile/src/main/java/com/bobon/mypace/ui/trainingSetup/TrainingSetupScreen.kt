package com.bobon.mypace.ui.trainingSetup


import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.bobon.mypace.modes.WalkingMode
import com.bobon.mypace.ui.TrainingViewModel
import com.bobon.mypace.ui.trainingSetup.components.GoalsDialog
import org.koin.androidx.compose.koinViewModel


@Composable
fun TrainingSetupScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: TrainingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    val mode by viewModel.activityMode.collectAsState()
    val isGoalSetupOpen by viewModel.isGoalSetupOpen.collectAsState()

    // Диалоги
    val showRationale by viewModel.showRationaleDialog.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showLocationServices by viewModel.showLocationServicesDialog.collectAsState()
    val showRetry by viewModel.showRetryDialog.collectAsState()
    val showPreciseLocation by viewModel.showPreciseLocationDialog.collectAsState()


    // Снэкбар
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.onPermissionResult(activity, results)
    }

    // Слушаем снэкбар сообщения
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val launchPermissionRequest by viewModel.launchPermissionRequest.collectAsState()

    LaunchedEffect(launchPermissionRequest) {
        if (launchPermissionRequest) {
            permissionLauncher.launch(viewModel.getRequiredPermissions())
            viewModel.onPermissionRequestLaunched()
        }
    }

    // При возврате из настроек
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.dismissDialogs()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // === ДИАЛОГИ ===

        // Rationale (перед первым запросом)
        if (showRationale) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissRationaleDialog() },
                title = { Text("Требуются разрешения") },
                text = { Text(viewModel.getPermissionRationaleText()) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onRationaleConfirm()
                        permissionLauncher.launch(viewModel.getRequiredPermissions())
                    }) {
                        Text("Понятно, продолжить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissRationaleDialog() }) {
                        Text("Позже")
                    }
                }
            )
        }
        if (showLocationServices) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissLocationDialog() },
                title = { Text("Геолокация выключена") },
                text = { Text(viewModel.getLocationDisabledText()) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onOpenLocationSettings() }) {
                        Text("Включить GPS")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissLocationDialog() }) {
                        Text("Позже")
                    }
                }
            )
        }

// Разрешения заблокированы
        if (showSettings) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissSettingsDialog() },
                title = { Text("Разрешения заблокированы") },
                text = { Text(viewModel.getPermissionsBlockedText()) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onOpenSettings() }) {
                        Text("Открыть настройки")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissSettingsDialog() }) {
                        Text("Позже")
                    }
                }
            )
        }
        if (showPreciseLocation) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissPreciseLocationDialog() },
                title = { Text("Нужна точная геолокация") },
                text = { Text(viewModel.getPreciseLocationRequiredText()) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onOpenAppSettingsForPrecise() }) {
                        Text("В настройки")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissPreciseLocationDialog() }) {
                        Text("Позже")
                    }
                }
            )
        }

        // Retry (после отказа, можно ещё раз объяснить)
        if (showRetry) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissRetryDialog() },
                title = { Text("Разрешение необходимо") },
                text = {
                    Text(
                        "Без точной геолокации приложение не может работать.\n\n" +
                                "Пожалуйста, предоставьте доступ."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onRetryRequest()
                        permissionLauncher.launch(viewModel.getRequiredPermissions())
                    }) {
                        Text("Предоставить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onOpenSettings() }) {
                        Text("В настройки")
                    }
                }
            )
        }



        // === UI ===
        TrainingSetupScreen(
            modifier = modifier,
            isWalking = mode == WalkingMode,
            isGoalSetupOpen = isGoalSetupOpen,
            onCloseGoalSetup = viewModel::closeGoalSetupDialog,
            onModeToggle = viewModel::changeMode,
            onOpenGoalSetup = viewModel::openGoalSetupDialog,
            onStartClick = { viewModel.onStartClick(activity) }
        )
    }
}

@Composable
fun TrainingSetupScreen(
    modifier: Modifier = Modifier,
    isWalking: Boolean,
    isGoalSetupOpen: Boolean,
    onCloseGoalSetup: () -> Unit,
    onModeToggle: () -> Unit,
    onOpenGoalSetup: () -> Unit,
    onStartClick: () -> Unit
) {


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

            GoalsDialog(isGoalSetupOpen, onCloseGoalSetup, {})

            SetupHeader()


            Spacer(Modifier.height(24.dp))

            ModeSelectionCard(
                isWalking = isWalking,
                onModeToggle = onModeToggle
            )

            Spacer(Modifier.height(16.dp))

            GoalsSelectionCard(
                onClick = onOpenGoalSetup
            )

            Spacer(Modifier.height(16.dp))

            QuickSettingsPlaceholder()

            Spacer(Modifier.weight(1f))

            StartWorkoutButton(
                onClick = onStartClick
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


@Preview(showBackground = true)
@Composable
private fun TrainingSetupScreenPreview() {
    MaterialTheme {
        TrainingSetupScreen(
            isWalking = false,
            isGoalSetupOpen = false,
            onCloseGoalSetup = {},
            onModeToggle = {},
            onOpenGoalSetup = {},
            onStartClick = {}
        )
    }
}
