package com.bobon.mypace.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun observeAllWorkouts():
            Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE activityType = :type ORDER BY startTime DESC ")
    fun observeAllWorkoutsByType(type: Int):
            Flow<List<WorkoutEntity>>


    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkoutById(id: String): WorkoutEntity?

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: String)

    @Query("SELECT COUNT(*) FROM workouts")
    fun observeWorkoutsCount():
            Flow<Int>

    @Query("SELECT COALESCE(SUM(totalDistance),0) FROM workouts")
    fun observeTotalDistance(): Flow<Float>

    @Query("SELECT COUNT(*) FROM workouts WHERE activityType = :type")
    fun observeWorkoutsCountByType(type: Int): Flow<Int>


    @Query("SELECT COALESCE(SUM(totalDistance), 0.0) FROM workouts  WHERE activityType = :type")
    fun observeTotalDistanceByType(type: Int): Flow<Double>


    @Query("SELECT COALESCE(SUM(endTime - startTime), 0) FROM workouts WHERE activityType = :type")
    fun observeTotalTimeByType(type: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(endTime - startTime), 0) FROM workouts")
    fun observeTotalTime(): Flow<Long>









}