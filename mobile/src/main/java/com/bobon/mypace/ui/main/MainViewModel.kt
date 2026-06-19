package com.bobon.mypace.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.usecase.logging.LogScreenChangedUseCase
import kotlinx.coroutines.launch

class MainViewModel(
    trainingManager: TrainingManager,
    private val logScreenChangedUseCase: LogScreenChangedUseCase
) : ViewModel() {

    val workoutState = trainingManager.workoutState

    fun logScreenChanged(screenName: String) {
        viewModelScope.launch {
            logScreenChangedUseCase(screenName)
        }
    }
}