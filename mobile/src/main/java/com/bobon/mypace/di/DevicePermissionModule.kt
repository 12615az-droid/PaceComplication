package com.bobon.mypace.di

import com.bobon.mypace.device.permission.AndroidPermissionManager
import com.bobon.mypace.domain.permission.PermissionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val devicePermissionModule = module {

    single<PermissionManager> {
        AndroidPermissionManager(androidContext())
    }
}