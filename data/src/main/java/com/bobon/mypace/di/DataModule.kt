package com.bobon.mypace.di

import androidx.room.Room
import com.bobon.mypace.data.repository.WorkoutRepositoryImpl
import com.bobon.mypace.data.database.AppDatabase
import com.bobon.mypace.domain.repository.WorkoutRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pace_db"
        ).build()
    }

    single { get<AppDatabase>().workoutDao() }

    single<WorkoutRepository> {
        WorkoutRepositoryImpl(get())
    }
}