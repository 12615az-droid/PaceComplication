package com.bobon.mypace.device.location.serviceLocation

import android.location.Location

interface LocationWrapper {
    fun startUpdates(callback: (Location) -> Unit)
    fun stopUpdates()
}