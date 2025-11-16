package com.example.a365fitness

import android.annotation.SuppressLint
import android.app.Application // <-- ADDED
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
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
import androidx.lifecycle.viewmodel.compose.viewModel // <-- ADDED
import com.example.a365fitness.ui.theme._365FitnessTheme

// --- ALL DATABASE IMPORTS ---
import com.example.a365fitness.database.database.Meal
import com.example.a365fitness.database.database.MeditationSession
import com.example.a365fitness.ui.viewmodel.FitnessViewModel
import com.example.a365fitness.ui.viewmodel.FitnessViewModelFactory

// --- ALL LOCAL DATA CLASSES AND SHARED PREFS ARE GONE ---

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale



// --- 1. MainActivity ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _365FitnessTheme {
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

// --- 2. Main Navigation ---

@Composable
fun MainAppContent(onLogout: () -> Unit) {
    val tabs = listOf("Dashboard", "Fitness", "Nutrition", "Mindfulness")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // --- VIEWMODEL INITIALIZATION ---
    // Get the Application context to create the factory
    val context = LocalContext.current
    val factory = FitnessViewModelFactory(context.applicationContext as Application)
    // Initialize the ViewModel. It will be shared across all screens.
    val viewModel: FitnessViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    // Check if this item is currently selected
                    val isSelected = selectedTab == index

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            // Determine which icon to show based on selection state
                            val icon = when (title) {
                                "Dashboard" -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                                "Fitness" -> if (isSelected) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter
                                "Nutrition" -> if (isSelected) Icons.Filled.Restaurant else Icons.Outlined.Restaurant
                                "Mindfulness" -> if (isSelected) Icons.Filled.SelfImprovement else Icons.Outlined.SelfImprovement
                                else -> Icons.Default.Favorite // Fallback
                            }
                            Icon(icon, contentDescription = title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (tabs[selectedTab]) {
                "Dashboard" -> DashboardScreen()
                // Pass the *same* ViewModel to each screen
                "Fitness" -> FitnessScreen(viewModel = viewModel)
                "Nutrition" -> NutritionScreen(viewModel = viewModel)
                "Mindfulness" -> MindfulnessScreen(viewModel = viewModel)
            }
        }
    }
}

// --- 3. Login Screen ---

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
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
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
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
                    progress = {0.72f},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// --- 6. Fitness Screen (REFACTORED) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // --- Get workout list from ViewModel ---
    val workouts by viewModel.allWorkouts.collectAsState()

    var showAddWorkoutDialog by rememberSaveable { mutableStateOf(false) }
    var newExercise by rememberSaveable { mutableStateOf("") }
    var newDuration by rememberSaveable { mutableStateOf("") }
    var newIntensity by rememberSaveable { mutableStateOf("Moderate") }

    Column(modifier.padding(16.dp)) {
        Text("Workout Log", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(items = workouts, key = { it.id }) { workout ->
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
                            onClick = {
                                // --- Use ViewModel to delete ---
                                viewModel.deleteWorkout(workout)
                                Toast.makeText(context, "Workout Deleted", Toast.LENGTH_SHORT).show()
                            }
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
                            // --- Use ViewModel to add ---
                            viewModel.addWorkout(newExercise, newDuration, newIntensity)

                            Toast.makeText(context, "Workout Added!", Toast.LENGTH_SHORT).show()
                            newExercise = ""
                            newDuration = ""
                            newIntensity = "Moderate"
                            showAddWorkoutDialog = false
                        } else {
                            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
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

// --- 7. Nutrition Screen (REFACTORED) ---

@Composable
fun NutritionScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // --- Get meal list from ViewModel ---
    val meals by viewModel.allMeals.collectAsState()

    var showAddMealDialog by rememberSaveable { mutableStateOf(false) }
    var newMealType by rememberSaveable { mutableStateOf("") }
    var newMealDescription by rememberSaveable { mutableStateOf("") }
    var newCalories by rememberSaveable { mutableStateOf("") }

    Column(modifier.padding(16.dp)) {
        Text("Meal Log", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = meals, key = { it.id }) { meal ->
                MealItem(
                    meal = meal,
                    onDelete = {
                        // --- Use ViewModel to delete ---
                        viewModel.deleteMeal(meal)
                        Toast.makeText(context, "Meal Deleted", Toast.LENGTH_SHORT).show()
                    }
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
                            // --- Use ViewModel to add ---
                            viewModel.addMeal(newMealType, newMealDescription, newCalories)

                            Toast.makeText(context, "Meal Added!", Toast.LENGTH_SHORT).show()
                            newMealType = ""
                            newMealDescription = ""
                            newCalories = ""
                            showAddMealDialog = false
                        } else {
                            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
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

// --- 8. Mindfulness Screen (REFACTORED) ---

@Composable
fun MindfulnessScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- Get history from ViewModel ---
    val meditationHistory by viewModel.allMeditations.collectAsState()

    var selectedDuration by rememberSaveable { mutableIntStateOf(5) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    var timeRemaining by rememberSaveable { mutableLongStateOf(selectedDuration * 60L) }
    var selectedMeditationType by rememberSaveable { mutableStateOf("Breathing") }

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
            val dateString = "Today, ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())}"
            // --- Use ViewModel to add ---
            viewModel.addMeditation(selectedMeditationType, "$completedMinutes", dateString)

            Toast.makeText(context, "Session Saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Session not long enough to save", Toast.LENGTH_SHORT).show()
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
                    progress = {if (selectedDuration > 0) {
                        1f - (timeRemaining.toFloat() / (selectedDuration * 60f))
                    } else 0f},
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


// --- 9. Utility Functions ---

// All SharedPreferences functions are removed.
// We only keep the time formatter.

@SuppressLint("DefaultLocale")
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}