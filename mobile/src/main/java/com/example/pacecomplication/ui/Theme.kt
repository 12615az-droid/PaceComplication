package com.example.pacecomplication.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * **AppTheme** — Обертка темы приложения на базе **Material 3**.
 *
 * ### Функционал:
 * - **Dynamic Color (Material You):** На Android 12+ адаптирует палитру под обои устройства.
 * - **Адаптивность:** Автоматическая поддержка темного/светлого режима системы.
 * - **Fallback:** Резервная схема для старых версий Android (ниже 12).
 *
 * ### Алгоритм работы:
 * 1. Проверка системной темы через [isSystemInDarkTheme].
 * 2. Если API >= 31 и `dynamicColor == true` — запуск [dynamicDarkColorScheme] или [dynamicLightColorScheme].
 * 3. В остальных случаях — применение стандартных схем проекта.
 * 4. Внедрение [MaterialTheme] для всего вложенного UI.
 *
 * ### Важные примечания для разработчика:
 * - ❌ **Не хардкодить цвета:** Избегайте `Color.White` или `Color.Black`.
 * - ✅ **Семантика:** Всегда используйте `MaterialTheme.colorScheme.*`.
 * - ✅ **Тонирование:** Для фонов экранов (например, настройки) отдавайте предпочтение
 * `surfaceVariant` или контейнерам вместо `background`.
 *
 * @param darkTheme Флаг темной темы (по умолчанию берется из системы).
 */
@Composable
fun RunningAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Следуем системной тёмной/светлой теме устройства
    content: @Composable () -> Unit // Контент приложения, который будет обёрнут в тему
) {
    val context = LocalContext.current
    val colorScheme = when {
        // Проверяем поддержку Android 12+
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme() // Твои дефолтные темные
        else -> lightColorScheme()    // Твои дефолтные светлые
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}