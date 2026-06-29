package com.bobon.mypace.domain.model


enum class WorkoutState(val code: Int) {
    IDLE(0),
    ACTIVE(1),
    PAUSED(2),
    FINISHED(3);

    companion object {
        fun fromCode(code: Int): WorkoutState? =
            entries.firstOrNull { it.code == code }
    }
}