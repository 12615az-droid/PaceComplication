package com.example.pacecomplication

import android.app.Application
import com.example.pacecomplication.di.appModule
import com.example.pacecomplication.logger.AppEventData
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.LogFilesManager
import com.example.pacecomplication.logger.SourceEvent
import com.example.pacecomplication.logger.TypeEvent
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