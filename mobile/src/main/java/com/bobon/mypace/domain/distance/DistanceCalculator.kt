package com.bobon.mypace.domain.distance

import com.bobon.mypace.domain.model.GpsPoint

interface DistanceCalculator {
    fun distanceBetween(
        from: GpsPoint,
        to: GpsPoint
    ): Double
}