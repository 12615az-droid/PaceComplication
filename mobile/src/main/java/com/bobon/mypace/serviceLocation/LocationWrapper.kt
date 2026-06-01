package com.bobon.mypace.serviceLocation

import android.location.Location

interface LocationWrapper {
    fun startUpdates(callback: (Location) -> Unit)
    fun stopUpdates()
}