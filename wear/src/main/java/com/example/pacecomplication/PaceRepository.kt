package com.example.pacecomplication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PaceRepository {
    private val _currentPace = MutableStateFlow("5:48")

    // А это публичная переменная, которую будут читать сервис и приложение
    val currentPace: StateFlow<String> = _currentPace

    fun updatePace(newPace: String) {
        _currentPace.value = newPace
    }
}
