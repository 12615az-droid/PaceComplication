package com.example.pacecomplication.presentation

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class PaceDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // 1. Проверяем, что это изменение данных и путь совпадает с тем, что мы слали
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/workout_sync") {

                // 2. Распаковываем данные
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val newPace = dataMap.getString("pace_key", "0:00")
                val newIsTracking = dataMap.getBoolean("is_tracking")
                val newWorkoutState = dataMap.getInt("workout_state")

                Log.d("WearData", newPace)
                Log.d("WearData", newIsTracking.toString())
                Log.d("WearData", newWorkoutState.toString())
                // 3. Обновляем репозиторий на часах
                PaceRepository.updateData(this, newPace, newIsTracking, newWorkoutState)


            }
        }
    }
}