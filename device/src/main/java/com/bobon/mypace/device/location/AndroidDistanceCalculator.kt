package com.bobon.mypace.device.location

import android.location.Location
import com.bobon.mypace.domain.distance.DistanceCalculator
import com.bobon.mypace.domain.model.GpsPoint

class AndroidDistanceCalculator : DistanceCalculator {

    override fun distanceBetween(
        from: GpsPoint,
        to: GpsPoint
    ): Double {
        val result = FloatArray(1)

        Location.distanceBetween(
            from.latitude,
            from.longitude,
            to.latitude,
            to.longitude,
            result
        )

        return result[0].toDouble()
    }
}