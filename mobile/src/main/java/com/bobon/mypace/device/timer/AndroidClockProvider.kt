package com.bobon.mypace.device.timer

import android.os.SystemClock
import com.bobon.mypace.domain.timer.ClockProvider

class AndroidClockProvider : ClockProvider {

    override fun elapsedRealtimeMillis(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}