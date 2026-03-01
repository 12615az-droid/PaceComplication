package com.example.pacecomplication.di

import SensorTracker
import com.example.pacecomplication.LocationNotificationHelper
import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.JsonlFileWriter
import com.example.pacecomplication.logger.LogFilesManager
import com.example.pacecomplication.logger.StateLogStorage
import com.example.pacecomplication.ui.TrainingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Объявляем Репозиторий как синглтон (один на всё приложение)
    single { LocationRepository(context = get(), eventsLog = get()) }
    single { LocationNotificationHelper(androidContext()) }
    single { SensorTracker(androidContext()) }
    single { LogFilesManager(androidContext()) }      // папка/cleanup/пути файлов
    single { JsonlFileWriter() }                      // appendLine()
    single { StateLogStorage(get(), get()) }          // files + writer
    single { EventsLog(get()) }                       // storage

    // 2. Объявляем Вьюмодель
    // get() сам найдет LocationRepository, потому что мы его объявили строчкой выше
    viewModel { TrainingViewModel(repository = get()) }
}