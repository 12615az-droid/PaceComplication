package com.bobon.mypace.domain.timer

interface ClockProvider {
    fun elapsedRealtimeMillis(): Long
    fun currentTimeMillis(): Long
}