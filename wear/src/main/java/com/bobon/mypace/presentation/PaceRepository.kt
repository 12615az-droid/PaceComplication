package com.bobon.mypace.presentation

import com.bobon.mypace.domain.model.WearTrainingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PaceRepository {

    private val _state = MutableStateFlow(WearTrainingState())
    val state: StateFlow<WearTrainingState> = _state.asStateFlow()

    fun updateState(state: WearTrainingState) {
        _state.value = state
    }
}