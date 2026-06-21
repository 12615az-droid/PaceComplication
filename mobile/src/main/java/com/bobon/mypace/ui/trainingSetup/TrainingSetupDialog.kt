package com.bobon.mypace.ui.trainingSetup

sealed interface TrainingSetupDialog {
    data object GoalSetup : TrainingSetupDialog
    data object PermissionRationale : TrainingSetupDialog
    data object PermissionSettings : TrainingSetupDialog
    data object LocationDisabled : TrainingSetupDialog
    data object PermissionRetry : TrainingSetupDialog
    data object PreciseLocationRequired : TrainingSetupDialog
}