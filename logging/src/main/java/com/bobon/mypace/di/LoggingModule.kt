package com.bobon.mypace.di

import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.GPSLog
import com.bobon.mypace.core.logger.JsonlFileWriter
import com.bobon.mypace.core.logger.LogFilesManager
import com.bobon.mypace.core.logger.SensorLog
import com.bobon.mypace.core.logger.StateLogStorage
import com.bobon.mypace.core.logger.TrainingEventLoggerImpl
import com.bobon.mypace.domain.logging.TrainingEventLogger
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val loggingModule = module {

    single {
        LogFilesManager(androidContext())
    }

    single {
        JsonlFileWriter()
    }

    single {
        StateLogStorage(get(), get())
    }

    single {
        EventsLog(get())
    }

    single<TrainingEventLogger> {
        TrainingEventLoggerImpl(
            eventsLog = get()
        )
    }

    single {
        GPSLog(get(), get())
    }

    single {
        SensorLog(get(), get())
    }
}