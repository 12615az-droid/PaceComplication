package com.bobon.mypace.utils

import java.math.BigDecimal
import java.math.RoundingMode


object DistanceFormatter {

    private const val DISTANCE_DEFAULT = "0.01"

    fun formatDistance(totalDistance: Double): String {
        if (totalDistance <= 0) return "0.00"
        if (totalDistance <= 10) return DISTANCE_DEFAULT
        return BigDecimal.valueOf(totalDistance / 1000)
            .setScale(2, RoundingMode.DOWN)
            .toString()
    }
}