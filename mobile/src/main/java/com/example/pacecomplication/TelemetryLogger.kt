package com.example.pacecomplication

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



/**
 * TelemetryLogger — вспомогательный логгер телеметрии.
 *
 * Используется для отладки:
 * - вывода диагностической информации в Logcat
 * - записи простого текстового лога в файл
 *
 * Не участвует в бизнес-логике приложения.
 * Может быть отключён или удалён без влияния на работу трекинга.
 */
class TelemetryLogger(private val context: Context) {
    private val logTag = "GPS_DEBUG"

    /**
     * Записывает отладочное сообщение.
     *
     * Формат:
     * - текущее время
     * - состояние батареи
     * - произвольный текст
     *
     * Сообщение выводится в Logcat и сохраняется в файл.
     *
     * @param text текст отладочного сообщения
     */
    fun log(text: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val battery = getBatteryInfo()
        val entry = "[$timestamp] $battery | $text"

        Log.d(logTag, entry)
        saveToFile(entry)
    }

    private fun saveToFile(text: String) {
        try {
            val file = File(context.filesDir, "gps_log_test.txt")
            file.appendText("$text\n")
        } catch (e: Exception) {
            Log.e(logTag, "Ошибка записи: ${e.message}")
        }
    }

    private fun getBatteryInfo(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val currentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000
        return "Bat: $capacity% | Draw: ${currentNow}mA"
    }
}

