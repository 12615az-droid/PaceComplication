package com.example.pacecomplication.presentation

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService

class PaceDataListenerService : WearableListenerService() {


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // 1. Проверяем, что это изменение данных и путь совпадает с тем, что мы слали
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/workout_sync") {

                // 2. Распаковываем данные
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val newPace = dataMap.getString("pace_key", "0:00")
                val newWorkoutState = dataMap.getInt("workout_state")

                Log.d("WearData", newPace)
                Log.d("WearData", newWorkoutState.toString())
                // 3. Обновляем репозиторий на часах
                PaceRepository.updateData(this, newPace, newWorkoutState)


            }
        }
    }


    override fun onPeerDisconnected(node: Node) {
        super.onPeerDisconnected(node)
        Log.d("WearData", "Связь с телефоном потеряна: ${node.displayName}")

        // Если телефон "отвалился", принудительно гасим сервис тренировки
        val intent = Intent(this, TrainingWearService::class.java).apply {
            action = "KILL"
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 3. Обязательно отменяем scope при уничтожении сервиса

    }
}