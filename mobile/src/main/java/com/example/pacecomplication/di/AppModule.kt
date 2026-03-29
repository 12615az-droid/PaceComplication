package com.example.pacecomplication.di

import GPSLog
import SensorLog
import SensorTracker
import com.example.pacecomplication.LocationNotificationHelper
import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.WearDataSender
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.JsonlFileWriter
import com.example.pacecomplication.logger.LogFilesManager
import com.example.pacecomplication.logger.StateLogStorage
import com.example.pacecomplication.pace.PaceCalculator
import com.example.pacecomplication.settings.DeveloperSettings
import com.example.pacecomplication.timer.PaceTimer
import com.example.pacecomplication.ui.TrainingViewModel
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
        ) 
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

    // 2. Объявляем Вьюмодель
    viewModel { TrainingViewModel(repository = get()) }
}