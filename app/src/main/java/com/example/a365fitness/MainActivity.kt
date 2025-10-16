package com.example.a365fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.a365fitness.ui.theme._365FitnessTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _365FitnessTheme {
                // New state to manage the login status
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    MainAppContent(
                        onLogout = { isLoggedIn = false } // Optional: for a future logout button
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

/**
 * Encapsulates the main application UI (Scaffold, BottomBar, Screens)
 */
@Composable
fun MainAppContent(onLogout: () -> Unit) {
    val tabs = listOf("Dashboard", "Fitness", "Nutrition", "Mindfulness")
    var selectedTab by remember { mutableStateOf(0) }

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
        // Use a Box to place the content within the padding
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

// --- New Login Screen Composable ---

/**
 * A basic, placeholder Login Screen.
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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

            // Login Button
            Button(
                onClick = {
                    // In a real app, you would validate credentials here (e.g., API call)
                    // For this example, any non-blank fields will "log in" successfully.
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

            // Placeholder for "Forgot Password" or "Sign Up"
            TextButton(onClick = { /* Handle Sign Up/Forgot Password */ }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}

// --- Existing Screen Composables (Adjusted to remove unused modifier.padding) ---

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

// Data class to represent a workout
data class Workout(
    val exercise: String,
    val duration: String,
    val intensity: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(modifier: Modifier = Modifier) {
    var workouts by remember { mutableStateOf(listOf(
        Workout("Running", "30 mins", "Moderate"),
        Workout("Lifting", "45 mins", "High") // Added examples for testing
    )) }

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var newExercise by remember { mutableStateOf("") }
    var newDuration by remember { mutableStateOf("") }
    var newIntensity by remember { mutableStateOf("Moderate") }

    // Function to delete a workout
    val deleteWorkout: (Workout) -> Unit = { workoutToDelete ->
        workouts = workouts.filter { it != workoutToDelete }
    }

    Column(modifier.padding(16.dp)) {
        Text("Workout Log", style = MaterialTheme.typography.headlineSmall)

        // Display workout list with a Delete button
        LazyColumn(
            modifier = Modifier.weight(1f) // Gives LazyColumn room to scroll
        ) {
            items(workouts) { workout ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Use Row to align text details and the delete button horizontally
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Details Column takes up most of the space
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exercise: ${workout.exercise}")
                            Text("Duration: ${workout.duration}")
                            Text("Intensity: ${workout.intensity}")
                        }

                        // Delete Button (IconButton)
                        IconButton(
                            onClick = { deleteWorkout(workout) }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Workout",
                                tint = MaterialTheme.colorScheme.error // Red color for delete
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add workout button (KEPT)
        Button(
            onClick = { showAddWorkoutDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Workout")
        }
    }

    // Add Workout Dialog (KEPT)
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
                                .menuAnchor(), // The correct Material 3 API is used here
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
                            workouts = workouts + newWorkout
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

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    var meals by remember {
        mutableStateOf(
            listOf(
                Meal("Breakfast", "Oats with fruits", "300 cal"),
                Meal("Lunch", "Salad with chicken", "450 cal"),
                Meal("Dinner", "Grilled fish with vegetables", "500 cal")
            )
        )
    }

    var showAddMealDialog by remember { mutableStateOf(false) }
    var newMealType by remember { mutableStateOf("") }
    var newMealDescription by remember { mutableStateOf("") }
    var newCalories by remember { mutableStateOf("") }

    // Function to delete a meal
    val deleteMeal: (Meal) -> Unit = { mealToDelete ->
        meals = meals.filter { it != mealToDelete }
    }

    Column(modifier.padding(16.dp)) {
        Text("Meal Log", style = MaterialTheme.typography.headlineSmall)

        // Display meal list with delete buttons
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

        // Add meal button
        Button(
            onClick = { showAddMealDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Meal")
        }
    }

    // Add Meal Dialog
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
                            meals = meals + newMeal
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

// Data class for meals
data class Meal(
    val mealType: String,
    val description: String,
    val calories: String
)

// Simple meal item with delete button
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

            // Delete button
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