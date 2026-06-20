package com.bobon.mypace.di


import com.bobon.mypace.domain.pace.PaceCalculator
import com.bobon.mypace.domain.timer.PaceTimer
import org.koin.dsl.module
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.usecase.logging.LogScreenChangedUseCase
import com.bobon.mypace.domain.usecase.training.ChangeTrainingModeUseCase
import com.bobon.mypace.domain.usecase.training.CheckStartWorkoutAvailabilityUseCase
import com.bobon.mypace.domain.usecase.training.ContinueTrainingUseCase
import com.bobon.mypace.domain.usecase.training.FinishTrainingUseCase
import com.bobon.mypace.domain.usecase.training.ObserveTrainingStateUseCase
import com.bobon.mypace.domain.usecase.training.PauseTrainingUseCase
import com.bobon.mypace.domain.usecase.training.SaveCurrentTrainingUseCase
import com.bobon.mypace.domain.usecase.training.StartTrainingUseCase
import com.bobon.mypace.domain.usecase.workout.DeleteWorkoutUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveTotalStatsUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutStatsUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsByTypeUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsUseCase
import com.bobon.mypace.domain.usecase.workout.SaveWorkoutUseCase
val domainModule = module {
    single {
        PaceTimer(
            clockProvider = get()
        )
    }

    single {
        PaceCalculator(
            stopThreshold = 0.5f,
            accBadThreshold = 35f
        )
    }
    single {
        CheckStartWorkoutAvailabilityUseCase(
            permissionManager = get()
        )
    }
    single {
        LogScreenChangedUseCase(
            eventsLog = get(),
            trainingManager = get()
        )
    }
    single {
        SaveCurrentTrainingUseCase(
            trainingManager = get(),
            saveWorkout = get(),
            finishTraining = get()
        )
    }
    single { ObserveWorkoutsUseCase(get()) }
    single { ObserveWorkoutsByTypeUseCase(get()) }
    single { ObserveWorkoutStatsUseCase(get()) }
    single { SaveWorkoutUseCase(get()) }
    single { DeleteWorkoutUseCase(get()) }
    single { ObserveTotalStatsUseCase(get()) }
    single {
        TrainingManager(
            paceTimer = get(),
            paceCalculator = get(),
            trainingSyncSender = get(),
            distanceCalculator = get()
        )
    }
    single {
        StartTrainingUseCase(
            trainingManager = get(),
            trainingServiceController = get()
        )
    }
    single {
        ObserveTrainingStateUseCase(
            trainingManager = get()
        )
    }

    single {
        ContinueTrainingUseCase(
            trainingManager = get(),
            startTraining = get()
        )
    }

    single {
        PauseTrainingUseCase(
            trainingManager = get(),
            trainingServiceController = get()
        )
    }

    single {
        FinishTrainingUseCase(
            trainingManager = get(),
            trainingServiceController = get()
        )
    }

    single {
        ChangeTrainingModeUseCase(
            trainingManager = get()
        )
    }
}