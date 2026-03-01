package com.example.pacecomplication

import android.app.Application
import com.example.pacecomplication.di.appModule
import com.example.pacecomplication.logger.LogFilesManager
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Пишем логи Koin в Logcat (поможет, если что-то пойдёт не так)
            androidLogger()
            // Передаём контекст приложения, чтобы репозитории могли им пользоваться
            androidContext(this@MainApplication)

            modules(appModule)
        }
        val logs = getKoin().get<LogFilesManager>()
        logs.ensureDirs()
        logs.cleanupOldAppLogs()
    }
}