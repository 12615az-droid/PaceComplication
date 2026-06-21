package com.bobon.mypace.di


import com.bobon.mypace.domain.pace.PaceCalculator
import com.bobon.mypace.domain.timer.PaceTimer
import com.bobon.mypace.domain.training.ActiveTrainingSession
import com.bobon.mypace.domain.training.TrainingCommandController
import com.bobon.mypace.domain.training.TrainingMetricsUpdater
import com.bobon.mypace.domain.training.TrainingStateReader
import org.koin.dsl.module
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.training.TrainingMetricsProcessor
import com.bobon.mypace.domain.training.TrainingStateHolder
import com.bobon.mypace.domain.training.TrainingSummaryFactory
import com.bobon.mypace.domain.usecase.logging.LogScreenChangedUseCase
import com.bobon.mypace.domain.usecase.permission.GetRequiredPermissionsUseCase
import com.bobon.mypace.domain.usecase.permission.HandlePermissionResultUseCase
import com.bobon.mypace.domain.usecase.permission.IsLocationEnabledUseCase
import com.bobon.mypace.domain.usecase.permission.MarkPermissionRationaleShownUseCase
import com.bobon.mypace.domain.usecase.permission.ShouldGoToSettingsUseCase
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
import com.bobon.mypace.domain.usecase.training.ObserveActivityModeUseCase
import com.bobon.mypace.domain.usecase.training.ObserveWorkoutStateUseCase
import org.koin.dsl.binds

val domainModule = module {
    single {
        PaceTimer(
            clockProvider = get()
        )
    }
    single {
        ObserveWorkoutStateUseCase(
            trainingStateReader = get()
        )
    }

    single {
        ObserveActivityModeUseCase(
            trainingStateReader = get()
        )
    }
    single {
        ShouldGoToSettingsUseCase(
            permissionManager = get()
        )
    }

    single {
        IsLocationEnabledUseCase(
            permissionManager = get()
        )
    }
    single {
        HandlePermissionResultUseCase(
            permissionManager = get()
        )
    }

    single {
        GetRequiredPermissionsUseCase(
            permissionManager = get()
        )
    }

    single {
        MarkPermissionRationaleShownUseCase(
            permissionManager = get()
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
            trainingStateReader = get()
        )
    }
    single {
        TrainingSummaryFactory(
            clockProvider = get()
        )
    }
    single {
        SaveCurrentTrainingUseCase(
            trainingStateReader = get(),
            trainingSummaryFactory = get(),
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
            stateHolder = get(),
            paceTimer = get(),
            metricsProcessor = get(),
            trainingSyncSender = get(),
            clockProvider = get()
        )
    } binds arrayOf(
        ActiveTrainingSession::class,
        TrainingStateReader::class,
        TrainingCommandController::class,
        TrainingMetricsUpdater::class
    )

    single {
        TrainingMetricsProcessor(
            paceCalculator = get(),
            distanceCalculator = get()
        )
    }
    single {
        TrainingStateHolder()
    }
    single {
        StartTrainingUseCase(
            trainingCommandController= get(),
            trainingServiceController = get()
        )
    }
    single {
        ObserveTrainingStateUseCase(
            trainingStateReader = get()
        )
    }

    single {
        ContinueTrainingUseCase(
            trainingStateReader = get(),
            startTraining = get()
        )
    }

    single {
        PauseTrainingUseCase(
            trainingCommandController= get(),
            trainingServiceController = get()
        )
    }

    single {
        FinishTrainingUseCase(
            trainingCommandController = get(),
            trainingServiceController = get()
        )
    }

    single {
        ChangeTrainingModeUseCase(
            trainingCommandController= get()
        )
    }
}
