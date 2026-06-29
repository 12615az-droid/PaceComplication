package com.bobon.mypace.di

import com.bobon.mypace.device.location.AndroidDistanceCalculator
import com.bobon.mypace.device.location.LocationUpdateHandler
import com.bobon.mypace.device.notification.LocationNotificationHelper
import com.bobon.mypace.device.service.AndroidTrainingServiceController
import com.bobon.mypace.domain.distance.DistanceCalculator
import com.bobon.mypace.domain.service.TrainingServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val deviceTrackingModule = module {

    single<DistanceCalculator> {
        AndroidDistanceCalculator()
    }

    single<TrainingServiceController> {
        AndroidTrainingServiceController(androidContext())
    }

    single {
        LocationNotificationHelper(androidContext())
    }
    single {
        LocationUpdateHandler(
            trainingMetricsUpdater = get(),
            trainingStateReader = get(),
            gpsLog = get()
        )
    }
}