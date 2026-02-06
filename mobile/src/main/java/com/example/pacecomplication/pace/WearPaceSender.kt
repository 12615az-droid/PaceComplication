package com.example.pacecomplication.pace

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

/**
 * WearPaceSender — отправка текущего темпа на Wear OS через DataClient.
 *
 * Использование:
 * - init(context) один раз, чтобы создать DataClient
 * - sendPace(paceString) для отправки значения
 */
class WearPaceSender {

    @SuppressLint("StaticFieldLeak")
    private var dataClient: DataClient? = null

    fun init(context: Context) {
        dataClient = Wearable.getDataClient(context.applicationContext)
    }

    fun sendPace(paceString: String) {
        dataClient?.let { client ->
            val putDataReq = PutDataMapRequest.create("/pace_updates").apply {
                dataMap.putString("pace_key", paceString)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
            client.putDataItem(putDataReq)
        }
    }
}