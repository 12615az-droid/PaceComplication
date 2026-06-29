package com.bobon.mypace.di

import com.bobon.mypace.device.wear.WearDataSender
import com.bobon.mypace.device.wear.WearTrainingSyncSender
import com.bobon.mypace.domain.training.TrainingSyncSender
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val deviceWearModule = module {

    single {
        WearDataSender(androidContext())
    }

    single<TrainingSyncSender> {
        WearTrainingSyncSender(get())
    }
}