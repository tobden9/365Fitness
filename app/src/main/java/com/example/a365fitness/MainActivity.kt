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
import androidx.compose.ui.unit.dp
import com.example.a365fitness.ui.theme._365FitnessTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _365FitnessTheme {
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
                    when (tabs[selectedTab]) {
                        "Dashboard" -> DashboardScreen(Modifier.padding(innerPadding))
                        "Fitness" -> FitnessScreen(Modifier.padding(innerPadding))
                        "Nutrition" -> NutritionScreen(Modifier.padding(innerPadding))
                        "Mindfulness" -> MindfulnessScreen(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

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
    var meal by remember { mutableStateOf("") }
    Column(modifier.padding(16.dp)) {
        Text("Log Your Meal", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = meal,
            onValueChange = { meal = it },
            label = { Text("Add a meal...") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        LazyColumn {
            items(listOf("Breakfast - Oats", "Lunch - Salad", "Dinner - Chicken")) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(item, Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun MindfulnessScreen(modifier: Modifier = Modifier) {
    Column(
        modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mindfulness Timer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { /* start timer */ }) {
            Text("Start 5-Minute Session")
        }
    }
}