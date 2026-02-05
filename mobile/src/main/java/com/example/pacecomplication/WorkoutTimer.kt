package com.example.pacecomplication


class WorkoutTimer {
    private var accumulatedMs: Long = 0
    private var startedAtMs: Long? = null


    fun startTimer(nowMs: Long) {
        if (startedAtMs != null) return
        startedAtMs = nowMs
    }

    fun onStop(nowMs: Long): Long {

        val start = startedAtMs ?: return accumulatedMs
        accumulatedMs += (nowMs - start)
        startedAtMs = null
        return accumulatedMs


    }

    fun currentMs(nowMs: Long): Long =
        startedAtMs?.let { accumulatedMs + (nowMs - it) }
            ?: accumulatedMs

    fun reset() {
        accumulatedMs = 0
        startedAtMs = null
    }

    fun formatTimer(totalMs: Long): String {
        val totalSeconds = totalMs / 1000

        return if (totalSeconds >= 3600) {
            "${totalSeconds / 3600}:" +
                    "%02d".format((totalSeconds / 60) % 60) + ":" +
                    "%02d".format(totalSeconds % 60)
        } else {
            "%02d".format((totalSeconds / 60) % 60) + ":" +
                    "%02d".format(totalSeconds % 60)
        }
    }

}