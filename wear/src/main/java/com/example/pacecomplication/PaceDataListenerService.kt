package com.example.pacecomplication

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService


class PaceDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // 1. Проверяем, что это изменение данных и путь совпадает с тем, что мы слали
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/pace_updates") {

                // 2. Распаковываем данные
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val newPace = dataMap.getString("pace_key", "0:00")

                // 3. Обновляем репозиторий на часах
                PaceRepository.updatePace(newPace)

                // 4. [ВАЖНО] Пингуем систему, чтобы виджет (Complication) перерисовался

            }
        }
    }
}


