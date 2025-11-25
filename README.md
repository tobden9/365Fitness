365Fitness

365Fitness is a comprehensive, holistic health tracking application built for Android using Kotlin and Jetpack Compose. It helps users track their physical workouts, nutritional intake, and mindfulness sessions in one unified interface.

Project Overview

This project demonstrates modern Android development practices, utilizing a declarative UI with Jetpack Compose and following the MVVM (Model-View-ViewModel) architectural pattern. It features a secure login system, data persistence via Room Database, and a visually engaging dashboard with gradient aesthetics.

Features

User Authentication:

Secure Sign Up and Login functionality.

Persistent login state using SharedPreferences.

Custom validation logic.

Dashboard:

Visual summary of daily statistics (Steps, Calories, Water, Sleep).

Motivation streak counter.

Responsive gradient cards and fluid UI elements.

Fitness Tracker:

Log workouts with Exercise name, Duration, and Intensity.

View history of workouts.

Delete capabilities.

Nutrition Log:

Track meals by Type (Breakfast, Lunch, etc.), Description, and Calories.

Manage daily caloric intake.

Mindfulness:

Built-in meditation timer (customizable duration).

Tracks session history (Date, Duration, Type).

Supports various modes: Breathing, Body Scan, Loving-Kindness, Visualization.


Tech Stack

Language: Kotlin

UI Framework: Jetpack Compose (Material3)

Architecture: MVVM (Model-View-ViewModel)

Local Storage: Room Database (SQLite)

Asynchronous Programming: Kotlin Coroutines & Flow

Testing: JUnit4, AndroidX Test (Instrumented Tests)

Architecture

The app follows the MVVM pattern to ensure separation of concerns and testability:

Model: Represents the data layer (Room Entities: User, Workout, Meal, MeditationSession).

View: The UI layer built with Jetpack Compose (MainActivity.kt screens).

ViewModel: Manages UI state and business logic (FitnessViewModel), communicating with the Repository/DAO.


Getting Started


git link [https://github.com/tobden9/365Fitness)




Testing

The project includes instrumented unit tests for the Room Database DAOs.


Future Improvements

Test-Driven Development (TDD): Adopting TDD for future features to ensure robust API definitions.

Cloud Sync: Migrating from local Room storage to Firebase Firestore for cross-device synchronization.
