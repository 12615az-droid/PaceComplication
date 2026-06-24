package com.bobon.mypace.presentation

import android.content.Intent
import android.util.Log
import com.bobon.mypace.presentation.sync.WearWorkoutSyncHandler
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService

class PaceDataListenerService : WearableListenerService() {


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/workout_sync"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val paceText = dataMap.getString("pace_key", "0:00")
                val workoutStateCode = dataMap.getInt("workout_state")

                Log.d("WearData", "pace=$paceText, state=$workoutStateCode")

                WearWorkoutSyncHandler.handleWorkoutUpdate(
                    context = this,
                    paceText = paceText,
                    workoutStateCode = workoutStateCode
                )
            }
        }
    }


    override fun onPeerDisconnected(node: Node) {
        super.onPeerDisconnected(node)

        Log.d("WearData", "Связь с телефоном потеряна: ${node.displayName}")

        WearWorkoutSyncHandler.handleDisconnect(this)
    }


}