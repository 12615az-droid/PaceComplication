package com.bobon.mypace.logger

import kotlinx.serialization.Serializable

@Serializable
enum class TypeEvent {
    APP_STARTED,
    SERVICE_STARTED,
    SERVICE_STOPPED,
    WORKOUT_STARTED,
    WORKOUT_STOPPED,
    MODE_CHANGED,
    FILTER_SELECTED,
    PERMISSION_RESULT,
    GPS_SIGNAL_CHANGED,
    ERROR,
    SCREEN_CHANGED,
}

@Serializable
enum class SourceEvent {
    UI,
    NOTIFICATION,
    SERVICE,
    SYSTEM,
    WEAR,
    UNKNOWN,
}