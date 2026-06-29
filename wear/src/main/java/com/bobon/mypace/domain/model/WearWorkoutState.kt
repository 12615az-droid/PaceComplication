package com.bobon.mypace.domain.model

enum class WearWorkoutState(val code: Int) {
    Idle(0),
    Active(1),
    Paused(2),
    Finished(3);

    companion object {
        fun fromCode(code: Int): WearWorkoutState {
            return entries.firstOrNull { it.code == code } ?: Idle
        }
    }
}