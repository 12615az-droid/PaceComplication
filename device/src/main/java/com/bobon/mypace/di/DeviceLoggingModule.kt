package com.bobon.mypace.di

import com.bobon.mypace.device.location.serviceLocation.LocationServiceEventLogger
import org.koin.dsl.module

val deviceLoggingModule = module {
    single {
        LocationServiceEventLogger(
            eventsLog = get(),
            trainingStateReader = get()
        )
    }
}