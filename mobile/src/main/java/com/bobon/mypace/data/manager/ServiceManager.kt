package com.bobon.mypace.data.manager

import android.content.Context
import android.content.Intent
import com.bobon.mypace.serviceLocation.LocationService

class ServiceManager(private val context: Context) {

    fun startService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = "START"
        }
        context.startForegroundService(intent)
    }

    fun stopService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = "STOP"
        }
        context.startForegroundService(intent)
    }

    fun killService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = "KILL"
        }
        context.startForegroundService(intent)
    }
}
