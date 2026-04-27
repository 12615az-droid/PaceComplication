package com.example.pacecomplication.serviceLocation

import android.location.Location

interface LocationWrapper {
    fun startUpdates(callback: (Location) -> Unit)
    fun stopUpdates()
}