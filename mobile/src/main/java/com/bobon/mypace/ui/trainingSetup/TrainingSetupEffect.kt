package com.bobon.mypace.ui.trainingSetup

sealed interface TrainingSetupEffect {
    data object RequestLocationPermission : TrainingSetupEffect
    data object OpenAppSettings : TrainingSetupEffect
    data object OpenLocationSettings : TrainingSetupEffect
}