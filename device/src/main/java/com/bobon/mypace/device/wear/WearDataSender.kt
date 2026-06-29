package com.bobon.mypace.device.wear

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

/**
 * WearDataSender — отправка данных о тренировке на Wear OS через DataClient.
 */
class WearDataSender(context: Context) {

    private val dataClient: DataClient = Wearable.getDataClient(context.applicationContext)

    fun sendWorkoutUpdate(paceString: String, workoutState: Int) {
        val putDataReq = PutDataMapRequest.create(WearSyncContract.PATH_WORKOUT_SYNC).apply {
            // Твой темп
            dataMap.putString(WearSyncContract.KEY_PACE, paceString)


            // Состояние тренировки
            dataMap.putInt(WearSyncContract.KEY_WORKOUT_STATE, workoutState)

            // Таймстемп важен, чтобы "та сторона" знала, что это свежие данные
            dataMap.putLong(WearSyncContract.KEY_TIMESTAMP, System.currentTimeMillis())
        }.asPutDataRequest()

        // Устанавливаем высокую приоритетность, чтобы данные летели сразу
        putDataReq.setUrgent()

        dataClient.putDataItem(putDataReq)
    }
}