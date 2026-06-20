package com.bobon.mypace.di

import SensorLog
import com.bobon.mypace.device.notification.LocationNotificationHelper
import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.GPSLog
import com.bobon.mypace.core.logger.JsonlFileWriter
import com.bobon.mypace.core.logger.LogFilesManager
import com.bobon.mypace.core.logger.StateLogStorage

import com.bobon.mypace.core.settings.DeveloperSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.bobon.mypace.device.wear.WearDataSender
import com.bobon.mypace.device.wear.WearTrainingSyncSender
import com.bobon.mypace.device.location.AndroidDistanceCalculator
import com.bobon.mypace.domain.distance.DistanceCalculator
import com.bobon.mypace.domain.training.TrainingSyncSender
import com.bobon.mypace.device.service.AndroidTrainingServiceController
import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.device.permission.AndroidPermissionManager
import com.bobon.mypace.device.permission.PermissionManager
import com.bobon.mypace.device.sensor.SensorTracker
import com.bobon.mypace.device.timer.AndroidClockProvider
import com.bobon.mypace.domain.timer.ClockProvider

val deviceModule = module {

    single<DistanceCalculator> {
        AndroidDistanceCalculator()
    }
    single<PermissionManager> {
        AndroidPermissionManager(androidContext())
    }

    single<TrainingServiceController> {
        AndroidTrainingServiceController(androidContext())
    }

    single {
        LocationNotificationHelper(androidContext())
    }

    single {
        SensorTracker(androidContext())
    }

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

    single {
        GPSLog(get(), get())
    }

    single {
        SensorLog(get(), get())
    }

    single {
        DeveloperSettings()
    }
    single<ClockProvider> {
        AndroidClockProvider()
    }

    single {
        WearDataSender(androidContext())
    }

    single<TrainingSyncSender> {
        WearTrainingSyncSender(get())
    }
}