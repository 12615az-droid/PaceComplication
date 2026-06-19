package com.bobon.mypace.di


import SensorLog
import SensorTracker
import androidx.room.Room
import com.bobon.mypace.LocationNotificationHelper
import com.bobon.mypace.WearDataSender
import com.bobon.mypace.data.manager.ServiceManager
import com.bobon.mypace.data.manager.TrainingManager
import com.bobon.mypace.database.AppDatabase
import com.bobon.mypace.logger.EventsLog
import com.bobon.mypace.logger.GPSLog
import com.bobon.mypace.logger.JsonlFileWriter
import com.bobon.mypace.logger.LogFilesManager
import com.bobon.mypace.logger.StateLogStorage
import com.bobon.mypace.pace.PaceCalculator
import com.bobon.mypace.permission.AndroidPermissionManager
import com.bobon.mypace.permission.PermissionManager
import com.bobon.mypace.settings.DeveloperSettings
import com.bobon.mypace.timer.PaceTimer
import com.bobon.mypace.ui.TrainingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.bobon.mypace.data.repository.RoomWorkoutRepository
import com.bobon.mypace.domain.repository.WorkoutRepository
val appModule = module {
    single { PaceTimer() }
    single { PaceCalculator(stopThreshold = 0.5f, accBadThreshold = 35f) }
    single { WearDataSender(androidContext()) }


    single<PermissionManager> { AndroidPermissionManager(get()) }

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pace_db"
        ).build()
    }

    single { get<AppDatabase>().workoutDao() }

    // Repositories & Managers
    single<WorkoutRepository> {
        RoomWorkoutRepository(get())
    }
    single { TrainingManager(get(), get(), get()) }
    single { ServiceManager(androidContext()) }

    // Infrastructure
    single { LocationNotificationHelper(androidContext()) }
    single { SensorTracker(androidContext()) }
    single { LogFilesManager(androidContext()) }
    single { JsonlFileWriter() }
    single { StateLogStorage(get(), get()) }
    single { EventsLog(get()) }
    single { GPSLog(get(), get()) }
    single { SensorLog(get(), get()) }
    single { DeveloperSettings() }

    // UI Layer
    viewModel {
        TrainingViewModel(
            trainingManager = get(),
            workoutRepository = get(),
            serviceManager = get(),
            eventsLog = get(),
            permissionManager = get ()
        )
    }
}
