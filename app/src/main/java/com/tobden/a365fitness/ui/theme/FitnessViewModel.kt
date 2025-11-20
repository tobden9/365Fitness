package com.tobden.a365fitness.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tobden.a365fitness.database.database.FitnessApplication
import com.tobden.a365fitness.database.database.FitnessRepository
import com.tobden.a365fitness.database.database.Meal
import com.tobden.a365fitness.database.database.MeditationSession
import com.tobden.a365fitness.database.database.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    // --- StateFlows for the UI to observe ---
    val allWorkouts: StateFlow<List<Workout>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allMeals: StateFlow<List<Meal>> = repository.allMeals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- THIS IS THE CORRECTED LINE ---
    val allMeditations: StateFlow<List<MeditationSession>> = repository.allMeditationSessions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Workout Functions ---
    fun addWorkout(exercise: String, duration: String, intensity: String) {
        viewModelScope.launch {
            repository.insertWorkout(
                Workout(exercise = exercise, duration = duration, intensity = intensity)
            )
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    // --- Meal Functions ---
    fun addMeal(type: String, description: String, calories: String) {
        viewModelScope.launch {
            repository.insertMeal(
                Meal(mealType = type, description = description, calories = calories)
            )
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    // --- Meditation Functions ---
    fun addMeditation(type: String, duration: String, date: String) {
        viewModelScope.launch {
            repository.insertMeditationSession(
                MeditationSession(type = type, duration = duration, date = date)
            )
        }
    }

    fun deleteMeditation(session: MeditationSession) {
        viewModelScope.launch {
            repository.deleteMeditationSession(session)
        }
    }
}

// --- ViewModel Factory ---
class FitnessViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            // Get the repository from your Application class
            val repository = (application as FitnessApplication).repository
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}