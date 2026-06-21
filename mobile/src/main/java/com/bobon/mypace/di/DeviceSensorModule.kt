package com.bobon.mypace.di

import com.bobon.mypace.device.sensor.SensorLoggingController
import com.bobon.mypace.device.sensor.SensorTracker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val deviceSensorModule = module {

    single {
        SensorTracker(androidContext())
    }
    single {
        SensorLoggingController(
            sensorTracker = get(),
            sensorLog = get(),
            trainingStateReader= get()
        )
    }


}