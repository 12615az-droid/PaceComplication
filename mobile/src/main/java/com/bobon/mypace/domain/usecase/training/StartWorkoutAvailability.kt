package com.bobon.mypace.domain.usecase.training

sealed interface StartWorkoutAvailability {
    data object Available : StartWorkoutAvailability
    data object NoLocationPermission : StartWorkoutAvailability
    data object OnlyCoarseLocation : StartWorkoutAvailability
    data object LocationDisabled : StartWorkoutAvailability
}