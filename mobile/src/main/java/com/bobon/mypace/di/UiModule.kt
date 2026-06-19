package com.bobon.mypace.di

import com.bobon.mypace.ui.TrainingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.bobon.mypace.ui.history.HistoryViewModel
val uiModule = module {
    viewModel {
        TrainingViewModel(
            trainingManager = get(),
            trainingServiceController = get(),
            eventsLog = get(),
            permissionManager = get(),
            startTraining = get(),
            pauseTraining = get(),
            finishTraining = get(),
            changeTrainingMode = get(),
                    observeWorkouts = get(),
            observeWorkoutsByType = get(),
            observeWorkoutStats = get(),
            saveWorkout = get(),
           observeTotalStats = get(),
            deleteWorkout = get()
        )
        HistoryViewModel(
            observeWorkouts = get(),
            observeWorkoutsByType = get(),
            observeTotalStats = get(),
            deleteWorkout = get()
        )
    }
}