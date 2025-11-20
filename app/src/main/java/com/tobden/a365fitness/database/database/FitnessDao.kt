package com.tobden.a365fitness.database.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // --- Workout Methods ---
    @Query("SELECT * FROM workout_table ORDER BY id DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // --- Meal Methods ---
    @Query("SELECT * FROM meal_table ORDER BY id DESC")
    fun getAllMeals(): Flow<List<Meal>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    // --- Meditation Methods ---
    @Query("SELECT * FROM meditation_table ORDER BY id DESC")
    fun getAllMeditationSessions(): Flow<List<MeditationSession>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeditationSession(session: MeditationSession)

    @Delete
    suspend fun deleteMeditationSession(session: MeditationSession)
}

