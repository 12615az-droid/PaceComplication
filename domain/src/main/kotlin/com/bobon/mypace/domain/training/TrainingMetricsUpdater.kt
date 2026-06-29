package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.GpsPoint
import com.bobon.mypace.domain.pace.PaceUpdate


interface TrainingMetricsUpdater {
    fun updatePace(point: GpsPoint): PaceUpdate?
}