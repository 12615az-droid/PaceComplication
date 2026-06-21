package com.bobon.mypace.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.domain.usecase.logging.LogScreenChangedUseCase
import com.bobon.mypace.domain.usecase.training.ObserveWorkoutStateUseCase
import kotlinx.coroutines.launch

class MainViewModel(
    observeWorkoutState: ObserveWorkoutStateUseCase,
    private val logScreenChangedUseCase: LogScreenChangedUseCase
) : ViewModel() {

    val workoutState = observeWorkoutState()

    fun logScreenChanged(screenName: String) {
        viewModelScope.launch {
            logScreenChangedUseCase(screenName)
        }
    }
}