package com.example.pacecomplication.ui

import androidx.lifecycle.ViewModel
import com.example.pacecomplication.LocationRepository

// Koin закинет сюда repository автоматически через get()
class TrainingViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    // Тут будет логика старта/стопа и расчеты.
    // Пока просто пустой класс для "галочки" в Koin.
}