package com.bobon.mypace.domain.training


interface TrainingCommandController {
    fun start(sessionId: String)
    fun pause()
    fun reset()
    fun changeMode()
}

