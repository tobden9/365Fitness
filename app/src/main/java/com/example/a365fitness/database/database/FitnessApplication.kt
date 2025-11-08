package com.example.a365fitness.database.database

import android.app.Application

class FitnessApplication : Application() {
    // Using 'lazy' ensures the database and repository are only created when needed
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { FitnessRepository(database.fitnessDao()) }
}

