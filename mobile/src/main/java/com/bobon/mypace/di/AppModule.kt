package com.bobon.mypace.di

import GPSLog
import SensorLog
import SensorTracker
import androidx.room.Room
import com.bobon.mypace.LocationNotificationHelper
import com.bobon.mypace.LocationRepository
import com.bobon.mypace.WearDataSender
import com.bobon.mypace.database.AppDatabase
import com.bobon.mypace.logger.EventsLog
import com.bobon.mypace.logger.JsonlFileWriter
import com.bobon.mypace.logger.LogFilesManager
import com.bobon.mypace.logger.StateLogStorage
import com.bobon.mypace.pace.PaceCalculator
import com.bobon.mypace.settings.DeveloperSettings
import com.bobon.mypace.timer.PaceTimer
import com.bobon.mypace.ui.TrainingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PaceTimer() }
    single { PaceCalculator(stopThreshold = 0.5f, accBadThreshold = 35f) }
    single { WearDataSender(androidContext()) }


    // 1. Объявляем Репозиторий как синглтон (один на всё приложение)
    single {
        LocationRepository(
            paceTimer = get(),
            paceCalculator = get(),
            wearDataSender = get(),
            context = get(),
            eventsLog = get()
                ,workoutDao = get ()
        )
    }



        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "pace_db"
            ).build()
        }

        single {
            get<AppDatabase>().workoutDao()
        }

        single { LocationNotificationHelper(androidContext()) }
        single { SensorTracker(androidContext()) }
        single { LogFilesManager(androidContext()) }      // папка/cleanup/пути файлов
        single { JsonlFileWriter() }                      // appendLine()
        single { StateLogStorage(get(), get()) }          // files + writer
        single { EventsLog(get()) }                       // storage
        single { GPSLog(get(), get()) }                    // gps stream logger
        single { SensorLog(get(), get()) }
        single { DeveloperSettings() }
        single {
            LogFilesManager(androidContext())
        }


        // 2. Объявляем Вьюмодель
        viewModel { TrainingViewModel(repository = get()) }
    }
