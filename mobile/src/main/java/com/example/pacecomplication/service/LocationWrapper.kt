package com.example.pacecomplication.service

import android.location.Location

interface LocationWrapper {
    fun startUpdates(callback: (Location) -> Unit)
    fun stopUpdates()
}