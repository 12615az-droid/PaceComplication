package com.bobon.mypace.core.logger

import com.bobon.mypace.domain.model.WorkoutState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface EventPayload

@Serializable
@SerialName("app")
data class AppEventData(
    val screen: String? = null,
    val workoutState: WorkoutState? = null,
    val permission: String? = null,
    val granted: Boolean? = null,
    val errorMessage: String? = null,
    val errorStack: String? = null,
    val note: String? = null
) : EventPayload

@Serializable
@SerialName("session")
data class SessionEventData(
    val workoutState: WorkoutState,
    val activityMode: String,
    val paceText: String? = null,
    val trainingTimeMs: Long? = null,
    val gpsAccuracyM: Float? = null,
    val note: String? = null
) : EventPayload