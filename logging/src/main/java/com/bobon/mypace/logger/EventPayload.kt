package com.bobon.mypace.logger

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface EventPayload

@Serializable
@SerialName("app")
data class AppEventData(
    val screen: String? = null,
    @SerialName("workoutStateCode")
    val workoutStateCode: Int? = null,
    val permission: String? = null,
    val granted: Boolean? = null,
    val errorMessage: String? = null,
    val errorStack: String? = null,
    val note: String? = null
) : EventPayload

@Serializable
@SerialName("session")
data class SessionEventData(
    @SerialName("workoutStateCode")
    val workoutStateCode: Int,
    val activityMode: String,
    val paceText: String? = null,
    val trainingTimeMs: Long? = null,
    val gpsAccuracyM: Float? = null,
    val note: String? = null
) : EventPayload