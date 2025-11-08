package com.example.a365fitness.database.database

import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {

    // --- Workout ---
    val allWorkouts: Flow<List<Workout>> = fitnessDao.getAllWorkouts()

    suspend fun insertWorkout(workout: Workout) {
        fitnessDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        fitnessDao.deleteWorkout(workout)
    }

    // --- Meal ---
    val allMeals: Flow<List<Meal>> = fitnessDao.getAllMeals()

    suspend fun insertMeal(meal: Meal) {
        fitnessDao.insertMeal(meal)
    }

    suspend fun deleteMeal(meal: Meal) {
        fitnessDao.deleteMeal(meal)
    }

    // --- Meditation ---
    val allMeditationSessions: Flow<List<MeditationSession>> = fitnessDao.getAllMeditationSessions()

    suspend fun insertMeditationSession(session: MeditationSession) {
        fitnessDao.insertMeditationSession(session)
    }

    // --- THIS WAS THE MISSING FUNCTION ---
    suspend fun deleteMeditationSession(session: MeditationSession) {
        fitnessDao.deleteMeditationSession(session)
    }
}