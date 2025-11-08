package com.example.a365fitness.database.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// --- ALL YOUR DATABASE TABLES (ENTITIES) ---

@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exercise: String,
    val duration: String,
    val intensity: String
)

@Entity(tableName = "meal_table")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "meal_type")
    val mealType: String,
    val description: String,
    val calories: String
)

@Entity(tableName = "meditation_table")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val duration: String,
    val date: String
)