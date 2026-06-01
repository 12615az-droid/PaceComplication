package com.bobon.mypace.pace

data class PaceUpdate(
    val paceValue: Double,   // для лога (например instant или ema)
    val paceText: String     // для уведомления
)