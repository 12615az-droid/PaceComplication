package com.example.pacecomplication

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

/**
 * WearDataSender — отправка данных о тренировке на Wear OS через DataClient.
 */
class WearDataSender(context: Context) {

    private val dataClient: DataClient = Wearable.getDataClient(context.applicationContext)

    fun sendWorkoutUpdate(paceString: String, isTracking: Boolean, workoutState: Int) {
        val putDataReq = PutDataMapRequest.create("/workout_sync").apply {
            // Твой темп
            dataMap.putString("pace_key", paceString)

            // Управление Старт/Стоп
            dataMap.putBoolean("is_tracking", isTracking)

            // Состояние тренировки
            dataMap.putInt("workout_state", workoutState)

            // Таймстемп важен, чтобы "та сторона" знала, что это свежие данные
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest()

        // Устанавливаем высокую приоритетность, чтобы данные летели сразу
        putDataReq.setUrgent()

        dataClient.putDataItem(putDataReq)
    }
}