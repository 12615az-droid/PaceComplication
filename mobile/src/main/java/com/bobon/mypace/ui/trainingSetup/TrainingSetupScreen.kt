package com.bobon.mypace.ui.trainingSetup

import android.Manifest

import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.bobon.mypace.domain.training.modes.WalkingMode
import com.bobon.mypace.ui.trainingSetup.components.GoalsDialog
import org.koin.androidx.compose.koinViewModel


@Composable
fun TrainingSetupScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: TrainingSetupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val mode by viewModel.activityMode.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val shouldShowFineLocationRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } ?: false

        viewModel.onPermissionResult(
            shouldShowFineLocationRationale = shouldShowFineLocationRationale
        )
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TrainingSetupEffect.RequestLocationPermission -> {
                    permissionLauncher.launch(viewModel.getRequiredPermissions())
                }

                TrainingSetupEffect.OpenAppSettings -> {
                    val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts(
                            "package",
                            context.packageName,
                            null
                        )
                    }
                    context.startActivity(intent)
                }

                TrainingSetupEffect.OpenLocationSettings -> {
                    context.startActivity(
                        Intent(AndroidSettings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.dismissDialog()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (activeDialog) {
            TrainingSetupDialog.PermissionRationale -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Требуются разрешения") },
                    text = { Text(TrainingSetupTexts.permissionRationaleText) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onRationaleConfirm() }) {
                            Text("Понятно, продолжить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Позже")
                        }
                    }
                )
            }

            TrainingSetupDialog.LocationDisabled -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Геолокация выключена") },
                    text = { Text(TrainingSetupTexts.locationDisabledText) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onOpenLocationSettings() }) {
                            Text("Включить GPS")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Позже")
                        }
                    }
                )
            }

            TrainingSetupDialog.PermissionSettings -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Разрешения заблокированы") },
                    text = { Text(TrainingSetupTexts.permissionsBlockedText) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onOpenSettings() }) {
                            Text("Открыть настройки")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Позже")
                        }
                    }
                )
            }

            TrainingSetupDialog.PreciseLocationRequired -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Нужна точная геолокация") },
                    text = { Text(TrainingSetupTexts.preciseLocationRequiredText) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onOpenAppSettingsForPrecise() }) {
                            Text("В настройки")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Позже")
                        }
                    }
                )
            }

            TrainingSetupDialog.PermissionRetry -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Разрешение необходимо") },
                    text = {
                        Text(
                            "Без точной геолокации приложение не может работать.\n\n" +
                                    "Пожалуйста, предоставьте доступ."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.dismissDialog()
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

            TrainingSetupDialog.GoalSetup,
            null -> Unit
        }

        TrainingSetupScreen(
            modifier = modifier,
            isWalking = mode == WalkingMode,
            isGoalSetupOpen = activeDialog == TrainingSetupDialog.GoalSetup,
            onCloseGoalSetup = viewModel::dismissDialog,
            onModeToggle = viewModel::changeMode,
            onOpenGoalSetup = viewModel::openGoalSetupDialog,
            onStartClick = {
                val shouldShowRationale = activity?.let { currentActivity ->
                    viewModel.getRequiredPermissions().any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            currentActivity,
                            permission
                        )
                    }
                } ?: false

                viewModel.onStartClick(shouldShowRationale)
            }
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
