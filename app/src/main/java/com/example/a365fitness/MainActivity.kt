package com.example.a365fitness

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.a365fitness.ui.theme._365FitnessTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

// --- 1. Data Classes ---

data class Workout(
    val exercise: String,
    val duration: String,
    val intensity: String
)

data class Meal(
    val mealType: String,
    val description: String,
    val calories: String
)

data class MeditationSession(
    val type: String,
    val duration: String,
    val date: String
)

// --- 2. MainActivity ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _365FitnessTheme {
                // Using rememberSaveable to persist login state across rotation
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }

                if (isLoggedIn) {
                    MainAppContent(
                        onLogout = { isLoggedIn = false }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true }
                    )
                }
            }
        }
    }
}

// --- 3. Main Navigation ---

@Composable
fun MainAppContent(onLogout: () -> Unit) {
    val tabs = listOf("Dashboard", "Fitness", "Nutrition", "Mindfulness")
    // Using rememberSaveable to persist the selected tab across rotation
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (tabs[selectedTab]) {
                "Dashboard" -> DashboardScreen()
                "Fitness" -> FitnessScreen()
                "Nutrition" -> NutritionScreen()
                "Mindfulness" -> MindfulnessScreen()
            }
        }
    }
}

// --- 4. Login Screen ---

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // Using rememberSaveable to persist login form data during rotation
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isLoginEnabled = username.isNotBlank() && password.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to 365Fitness",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLoginEnabled) {
                        onLoginSuccess()
                    }
                },
                enabled = isLoginEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Handle Sign Up/Forgot Password */ }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}

// --- 5. Dashboard Screen ---

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Text("Today's Progress", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Steps: 7,230 / 10,000")
                LinearProgressIndicator(
                    progress = 0.72f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// --- 6. Fitness Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var workouts by remember { mutableStateOf(loadWorkoutHistory(context)) }

    // Using rememberSaveable for dialog and form states
    var showAddWorkoutDialog by rememberSaveable { mutableStateOf(false) }
    var newExercise by rememberSaveable { mutableStateOf("") }
    var newDuration by rememberSaveable { mutableStateOf("") }
    var newIntensity by rememberSaveable { mutableStateOf("Moderate") }

    val deleteWorkout: (Workout) -> Unit = { workoutToDelete ->
        val updatedWorkouts = workouts.filter { it != workoutToDelete }
        workouts = updatedWorkouts
        saveWorkoutHistory(context, updatedWorkouts)
    }

    Column(modifier.padding(16.dp)) {
        Text("Workout Log", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(workouts) { workout ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exercise: ${workout.exercise}")
                            Text("Duration: ${workout.duration}")
                            Text("Intensity: ${workout.intensity}")
                        }

                        IconButton(
                            onClick = { deleteWorkout(workout) }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Workout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddWorkoutDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Workout")
        }
    }

    if (showAddWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showAddWorkoutDialog = false },
            title = { Text("Add New Workout") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newExercise,
                        onValueChange = { newExercise = it },
                        label = { Text("Exercise") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("Duration (e.g., 30 mins)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = newIntensity,
                            onValueChange = {},
                            label = { Text("Intensity") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Light", "Moderate", "High").forEach { intensity ->
                                DropdownMenuItem(
                                    text = { Text(intensity) },
                                    onClick = {
                                        newIntensity = intensity
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newExercise.isNotBlank() && newDuration.isNotBlank()) {
                            val newWorkout = Workout(newExercise, newDuration, newIntensity)
                            val updatedWorkouts = workouts + newWorkout
                            workouts = updatedWorkouts
                            saveWorkoutHistory(context, updatedWorkouts)

                            newExercise = ""
                            newDuration = ""
                            newIntensity = "Moderate"
                            showAddWorkoutDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddWorkoutDialog = false
                        newExercise = ""
                        newDuration = ""
                        newIntensity = "Moderate"
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- 7. Nutrition Screen ---

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var meals by remember { mutableStateOf(loadMealHistory(context)) }

    // Using rememberSaveable for dialog and form states
    var showAddMealDialog by rememberSaveable { mutableStateOf(false) }
    var newMealType by rememberSaveable { mutableStateOf("") }
    var newMealDescription by rememberSaveable { mutableStateOf("") }
    var newCalories by rememberSaveable { mutableStateOf("") }

    val deleteMeal: (Meal) -> Unit = { mealToDelete ->
        val updatedMeals = meals.filter { it != mealToDelete }
        meals = updatedMeals
        saveMealHistory(context, updatedMeals)
    }

    Column(modifier.padding(16.dp)) {
        Text("Meal Log", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(
                items = meals,
                key = { meal -> "${meal.mealType}-${meal.description}-${meal.calories}" }
            ) { meal ->
                MealItem(
                    meal = meal,
                    onDelete = { deleteMeal(meal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddMealDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Meal")
        }
    }

    if (showAddMealDialog) {
        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            title = { Text("Add New Meal") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newMealType,
                        onValueChange = { newMealType = it },
                        label = { Text("Meal Type (e.g., Breakfast, Lunch)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newMealDescription,
                        onValueChange = { newMealDescription = it },
                        label = { Text("Meal Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newCalories,
                        onValueChange = { newCalories = it },
                        label = { Text("Calories") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMealType.isNotBlank() && newMealDescription.isNotBlank() && newCalories.isNotBlank()) {
                            val newMeal = Meal(newMealType, newMealDescription, newCalories)
                            val updatedMeals = meals + newMeal
                            meals = updatedMeals
                            saveMealHistory(context, updatedMeals)

                            newMealType = ""
                            newMealDescription = ""
                            newCalories = ""
                            showAddMealDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddMealDialog = false
                        newMealType = ""
                        newMealDescription = ""
                        newCalories = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MealItem(
    meal: Meal,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(all = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Meal: ${meal.mealType}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Description: ${meal.description}")
                Text(text = "Calories: ${meal.calories}")
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Meal",
                    tint = Color.Red
                )
            }
        }
    }
}

// --- 8. Mindfulness Screen ---

@Composable
fun MindfulnessScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Using rememberSaveable to persist timer state across rotation
    var selectedDuration by rememberSaveable { mutableStateOf(5) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    var timeRemaining by rememberSaveable { mutableStateOf(selectedDuration * 60L) }
    var selectedMeditationType by rememberSaveable { mutableStateOf("Breathing") }

    var meditationHistory by remember {
        mutableStateOf(loadMeditationHistory(context))
    }

    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(java.util.Date())
    }

    val currentTime = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())
    }

    val meditationTypes = listOf("Breathing", "Body Scan", "Loving-Kindness", "Visualization")

    fun saveMeditationSession() {
        val completedSeconds = (selectedDuration * 60L) - timeRemaining
        val completedMinutes = (completedSeconds / 60).toInt()

        if (completedMinutes > 0) {
            val newSession = MeditationSession(
                selectedMeditationType,
                "$completedMinutes",
                "Today, ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())}"
            )
            val newHistory = listOf(newSession) + meditationHistory
            meditationHistory = newHistory
            saveMeditationHistory(context, newHistory)
        }
    }

    LaunchedEffect(timerRunning, timeRemaining) {
        if (timerRunning) {
            while (timeRemaining > 0 && timerRunning) {
                delay(1000L)
                timeRemaining--
            }
            if (timeRemaining == 0L) {
                timerRunning = false
                saveMeditationSession()
            }
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // Meditation Type
        Text(
            text = "Meditation Type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(meditationTypes) { type ->
                FilterChip(
                    selected = selectedMeditationType == type,
                    onClick = { selectedMeditationType = type },
                    label = { Text(type) }
                )
            }
        }

        // Timer Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Remaining Time",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatTime(timeRemaining),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = if (selectedDuration > 0) {
                        1f - (timeRemaining.toFloat() / (selectedDuration * 60f))
                    } else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                val completedMinutesDisplay = selectedDuration - (timeRemaining / 60)
                Text(
                    text = "Completed: ${if (completedMinutesDisplay > 0) completedMinutesDisplay else 0} min",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Duration Selection
        Text(
            text = "Session Duration",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf(1, 5, 10, 15, 20, 30)) { minutes ->
                AssistChip(
                    onClick = {
                        if (!timerRunning) {
                            selectedDuration = minutes
                            timeRemaining = minutes * 60L
                        }
                    },
                    label = { Text("$minutes min") }
                )
            }
        }

        // Timer Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { timerRunning = !timerRunning },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (timerRunning) "Pause" else "Start")
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = {
                        if (timerRunning || timeRemaining < selectedDuration * 60L) {
                            saveMeditationSession()
                        }
                        timerRunning = false
                        timeRemaining = selectedDuration * 60L
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (timerRunning) {
                Button(
                    onClick = {
                        timerRunning = false
                        saveMeditationSession()
                        timeRemaining = selectedDuration * 60L
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop & Save")
                }
            }
        }

        // Meditation History
        if (meditationHistory.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    meditationHistory.forEach { session ->
                        MeditationHistoryItem(session = session)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No meditation sessions yet",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Start your first session above!",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MeditationHistoryItem(session: MeditationSession) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = session.duration,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


// --- 9. Persistence and Utility Functions ---

private object PersistenceKeys {
    const val PREFS_NAME = "FitnessAppPrefs"
    const val WORKOUT_HISTORY_KEY = "WorkoutHistory"
    const val MEAL_HISTORY_KEY = "MealHistory"
    const val MINDFULNESS_HISTORY_KEY = "MindfulnessHistory"
}

private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PersistenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// --- Workout Persistence ---

private fun loadWorkoutHistory(context: Context): List<Workout> {
    val json = getPrefs(context).getString(PersistenceKeys.WORKOUT_HISTORY_KEY, null)
    return if (json != null) {
        val type = object : TypeToken<List<Workout>>() {}.type
        Gson().fromJson(json, type)
    } else {
        listOf(Workout("Running", "30 mins", "Moderate"), Workout("Lifting", "45 mins", "High"))
    }
}

private fun saveWorkoutHistory(context: Context, history: List<Workout>) {
    val json = Gson().toJson(history)
    getPrefs(context).edit().putString(PersistenceKeys.WORKOUT_HISTORY_KEY, json).apply()
}

// --- Meal Persistence ---

private fun loadMealHistory(context: Context): List<Meal> {
    val json = getPrefs(context).getString(PersistenceKeys.MEAL_HISTORY_KEY, null)
    return if (json != null) {
        val type = object : TypeToken<List<Meal>>() {}.type
        Gson().fromJson(json, type)
    } else {
        listOf(Meal("Breakfast", "Oats with fruits", "300 cal"), Meal("Lunch", "Salad with chicken", "450 cal"), Meal("Dinner", "Grilled fish with vegetables", "500 cal"))
    }
}

private fun saveMealHistory(context: Context, history: List<Meal>) {
    val json = Gson().toJson(history)
    getPrefs(context).edit().putString(PersistenceKeys.MEAL_HISTORY_KEY, json).apply()
}

// --- Meditation Persistence ---

private fun loadMeditationHistory(context: Context): List<MeditationSession> {
    val json = getPrefs(context).getString(PersistenceKeys.MINDFULNESS_HISTORY_KEY, null)
    return if (json != null) {
        val type = object : TypeToken<List<MeditationSession>>() {}.type
        Gson().fromJson(json, type)
    } else {
        listOf(MeditationSession("Breathing", "5", "Today, 09:30 AM"), MeditationSession("Body Scan", "10", "Yesterday, 08:15 PM"))
    }
}

private fun saveMeditationHistory(context: Context, history: List<MeditationSession>) {
    val json = Gson().toJson(history)
    getPrefs(context).edit().putString(PersistenceKeys.MINDFULNESS_HISTORY_KEY, json).apply()
}