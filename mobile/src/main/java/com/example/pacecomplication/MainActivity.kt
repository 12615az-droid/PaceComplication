package com.example.pacecomplication


import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pacecomplication.ui.PaceScreen
import com.example.pacecomplication.ui.RunningAppTheme


/**
 * MainActivity — точка входа приложения.
 *
 * Ответственность Activity:
 * - запрос runtime-разрешений (геолокация + уведомления на Android 13+)
 * - запуск/остановка фонового сервиса трекинга (LocationService)
 * - установка Compose UI (Theme -> Surface -> PaceScreen)
 *
 * Важно:
 * - бизнес-состояние трекинга хранится в LocationRepository
 * - UI получает события через callbacks (onStart/onStop/onModeChanged)
 */
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose-дерево приложения: тема -> контейнер -> основной экран
        setContent {

            RunningAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Передаём в UI только события управления.
                    // Сама Activity решает: запросить разрешения, запустить/остановить сервис и т.д
                    PaceScreen()


                }
            }
        }


    }


}