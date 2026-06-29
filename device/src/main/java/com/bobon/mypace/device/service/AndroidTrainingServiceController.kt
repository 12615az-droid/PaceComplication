package com.bobon.mypace.device.service

import android.content.Context
import android.content.Intent
import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.device.location.serviceLocation.LocationService

class AndroidTrainingServiceController(
    private val context: Context
) : TrainingServiceController {

    override fun startTrackingService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = TrainingServiceActions.START
        }
        context.startForegroundService(intent)
    }

    override fun stopTrackingService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = TrainingServiceActions.STOP
        }
        context.startForegroundService(intent)
    }

    override fun killTrackingService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = TrainingServiceActions.KILL
        }
        context.startForegroundService(intent)
    }
}