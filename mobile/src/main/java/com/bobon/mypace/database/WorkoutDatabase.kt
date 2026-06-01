package com.bobon.mypace.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities  = [WorkoutEntity::class], version = 1, exportSchema = false
)


abstract class AppDatabase: RoomDatabase(){
    abstract fun workoutDao(): WorkoutDao
}