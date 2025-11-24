package com.tobden.a365fitness

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tobden.a365fitness.database.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test for the Room database and DAO.
 * Runs on a real or virtual Android device/emulator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FitnessDatabaseTest {

    private lateinit var fitnessDao: FitnessDao
    private lateinit var db: AppDatabase

    // Setup runs before each test
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory database builder for testing:
        // the database is created in memory and destroyed after the tests finish.
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        fitnessDao = db.fitnessDao()
    }

    // Teardown runs after each test
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // --- 1. Workout Tests ---

    @Test
    @Throws(Exception::class)
    fun insertAndGetAllWorkouts() = runTest {
        val workout1 = Workout(exercise = "Running", duration = "30 mins", intensity = "Moderate")
        val workout2 = Workout(exercise = "Lifting", duration = "60 mins", intensity = "High")

        fitnessDao.insertWorkout(workout1)
        fitnessDao.insertWorkout(workout2)

        // Flow.first() collects the first emitted value and cancels the flow
        val allWorkouts = fitnessDao.getAllWorkouts().first()

        assertEquals(2, allWorkouts.size)
        // Check if the latest inserted item (Lifting) is first because of ORDER BY id DESC
        assertEquals("Lifting", allWorkouts[0].exercise)
        assertEquals("Running", allWorkouts[1].exercise)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteWorkout() = runTest {
        val workout = Workout(exercise = "Yoga", duration = "45 mins", intensity = "Light")

        fitnessDao.insertWorkout(workout)
        // Get the actual inserted object (with generated ID) to delete it
        val insertedWorkout = fitnessDao.getAllWorkouts().first().first()

        fitnessDao.deleteWorkout(insertedWorkout)

        val allWorkouts = fitnessDao.getAllWorkouts().first()
        assertTrue(allWorkouts.isEmpty())
    }

    // --- 2. Meal Tests ---

    @Test
    @Throws(Exception::class)
    fun insertAndGetAllMeals() = runTest {
        val meal1 = Meal(mealType = "Breakfast", description = "Oatmeal", calories = "300")
        val meal2 = Meal(mealType = "Lunch", description = "Salad", calories = "450")

        fitnessDao.insertMeal(meal1)
        fitnessDao.insertMeal(meal2)

        val allMeals = fitnessDao.getAllMeals().first()

        assertEquals(2, allMeals.size)
        // Check order: Lunch was inserted second, should be first
        assertEquals("Lunch", allMeals[0].mealType)
    }

    // --- 3. Meditation Tests ---

    @Test
    @Throws(Exception::class)
    fun insertAndGetAllMeditationSessions() = runTest {
        val session1 = MeditationSession(type = "Breathing", duration = "10", date = "Today")
        val session2 = MeditationSession(type = "Body Scan", duration = "20", date = "Yesterday")

        fitnessDao.insertMeditationSession(session1)
        fitnessDao.insertMeditationSession(session2)

        val allSessions = fitnessDao.getAllMeditationSessions().first()

        assertEquals(2, allSessions.size)
        // Check order: Body Scan was inserted second, should be first
        assertEquals("Body Scan", allSessions[0].type)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteMeditationSession() = runTest {
        val session = MeditationSession(type = "Visualization", duration = "5", date = "Now")

        fitnessDao.insertMeditationSession(session)
        // Get the actual inserted object (with generated ID) to delete it
        val insertedSession = fitnessDao.getAllMeditationSessions().first().first()

        fitnessDao.deleteMeditationSession(insertedSession)

        val allSessions = fitnessDao.getAllMeditationSessions().first()
        assertTrue(allSessions.isEmpty())
    }

    // --- 4. User Tests (Fixed) ---

    @Test
    @Throws(Exception::class)
    fun registerAndGetUserSuccess() = runTest {
        // Create a new user
        val user = User(username = "TestUser1", password = "Password123")

        // Register (Insert)
        fitnessDao.insertUser(user)

        // FIX: Use getUser with ONLY the username (as per your DAO error)
        val retrievedUser = fitnessDao.getUser("TestUser1")

        // Assert that we found the user
        assertNotNull("User should be found", retrievedUser)
        // Verify the password was saved correctly
        assertEquals("Password123", retrievedUser?.password)
    }

    @Test
    @Throws(Exception::class)
    fun getUserNotFound() = runTest {
        // Test querying a user that doesn't exist
        val nonExistentUser = fitnessDao.getUser("GhostUser")

        // Assert that the result is null
        assertNull("User should not be found", nonExistentUser)
    }

    @Test
    @Throws(Exception::class)
    fun checkUsernameAlreadyExists() = runTest {
        // Create user
        val user = User(username = "ExistingUser", password = "123")
        fitnessDao.insertUser(user)

        // FIX: Use getUser instead of getUserByUsername
        val existingUser = fitnessDao.getUser("ExistingUser")
        val ghostUser = fitnessDao.getUser("GhostUser")

        // Assert
        assertNotNull("Should find existing username", existingUser)
        assertNull("Should not find non-existing username", ghostUser)
    }
}