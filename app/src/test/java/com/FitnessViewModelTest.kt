package com.tobden.a365fitness

import com.tobden.a365fitness.database.database.FitnessRepository
import com.tobden.a365fitness.database.database.User
import com.tobden.a365fitness.database.database.Workout
import com.tobden.a365fitness.database.database.Meal
import com.tobden.a365fitness.database.database.MeditationSession
import com.tobden.a365fitness.ui.viewmodel.FitnessViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

// --- KEY FIX: Use these specific imports for Kotlin ---
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
class FitnessViewModelTest {

    @Mock
    private lateinit var repository: FitnessRepository

    private lateinit var viewModel: FitnessViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Fix: Use 'whenever' instead of 'when'
        whenever(repository.allWorkouts).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.allMeals).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.allMeditationSessions).thenReturn(MutableStateFlow(emptyList()))

        viewModel = FitnessViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registerUser returns true when user does not exist`() = runTest(testDispatcher) {
        val newUser = User(username = "newuser", password = "password")

        // Fix: Use 'whenever'
        whenever(repository.getUserByUsername("newuser")).thenReturn(null)

        var result: Boolean? = null
        viewModel.registerUser(newUser) { success ->
            result = success
        }

        testScheduler.advanceUntilIdle()

        // This 'verify' should now work correctly
        verify(repository).registerUser(newUser)
        assertTrue(result == true)
    }

    @Test
    fun `registerUser returns false when user already exists`() = runTest(testDispatcher) {
        val existingUser = User(username = "existing", password = "password")

        whenever(repository.getUserByUsername("existing")).thenReturn(existingUser)

        var result: Boolean? = null
        viewModel.registerUser(existingUser) { success ->
            result = success
        }

        testScheduler.advanceUntilIdle()

        assertTrue(result == false)
    }

    @Test
    fun `loginUser returns Success for correct credentials`() = runTest(testDispatcher) {
        val user = User(username = "john", password = "123")
        whenever(repository.getUserByUsername("john")).thenReturn(user)

        var loginResult = ""
        viewModel.loginUser("john", "123") { msg ->
            loginResult = msg
        }

        testScheduler.advanceUntilIdle()

        assertEquals("Success", loginResult)
    }

    @Test
    fun `addWorkout calls repository insert`() = runTest(testDispatcher) {
        viewModel.addWorkout("Pushups", "10 min", "High")
        testScheduler.advanceUntilIdle()

        // We verify that insertWorkout was called with ANY workout object
        verify(repository).insertWorkout(any())
    }

    @Test
    fun `deleteWorkout calls repository delete`() = runTest(testDispatcher) {
        val workout = Workout(id = 1, exercise = "Run", duration = "30m", intensity = "Low")
        viewModel.deleteWorkout(workout)
        testScheduler.advanceUntilIdle()
        verify(repository).deleteWorkout(workout)
    }
}