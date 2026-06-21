package com.bobon.mypace.di

import com.bobon.mypace.device.timer.AndroidClockProvider
import com.bobon.mypace.domain.timer.ClockProvider
import org.koin.dsl.module

val deviceTimerModule = module {

    single<ClockProvider> {
        AndroidClockProvider()
    }
}