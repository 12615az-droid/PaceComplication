package com.example.pacecomplication // проверь свой пакет!

import android.app.Application
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
            // Список модулей (пока оставим пустым, наполним через минуту)
            modules(emptyList())
        }
    }
}