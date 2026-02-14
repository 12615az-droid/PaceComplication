package com.example.pacecomplication.di

import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.ui.TrainingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Объявляем Репозиторий как синглтон (один на всё приложение)
    single { LocationRepository(get()) }

    // 2. Объявляем Вьюмодель
    // get() сам найдет LocationRepository, потому что мы его объявили строчкой выше
    viewModel { TrainingViewModel(repository = get()) }
}