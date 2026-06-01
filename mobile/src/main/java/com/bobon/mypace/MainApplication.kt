package com.bobon.mypace

import android.app.Application
import com.bobon.mypace.di.appModule
import com.bobon.mypace.logger.AppEventData
import com.bobon.mypace.logger.EventsLog
import com.bobon.mypace.logger.LogFilesManager
import com.bobon.mypace.logger.SourceEvent
import com.bobon.mypace.logger.TypeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
        logs.cleanupOldLogs()
        val eventsLog = getKoin().get<EventsLog>()
        appScope.launch {
            eventsLog.log(
                type = TypeEvent.APP_STARTED,
                source = SourceEvent.SYSTEM,
                origin = "MainApplication.onCreate",
                data = AppEventData(
                    note = "Application created"
                )
            )
        }
    }
}