package com.tobden.a365fitness

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tobden.a365fitness.database.database.Meal
import com.tobden.a365fitness.database.database.MeditationSession
import com.tobden.a365fitness.database.database.User
import com.tobden.a365fitness.ui.theme._365FitnessTheme
import com.tobden.a365fitness.ui.viewmodel.FitnessViewModel
import com.tobden.a365fitness.ui.viewmodel.FitnessViewModelFactory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

// Define the gradient based on your logo
val LogoGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF009688), // Teal
        Color(0xFF03A9F4)  // Blue/Cyan
    )
)

// --- 1. MainActivity ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _365FitnessTheme {
                val context = LocalContext.current

                // Initialize ViewModel
                val factory = FitnessViewModelFactory(context.applicationContext as Application)
                val viewModel: FitnessViewModel = viewModel(factory = factory)

                // Persistent Login State
                val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                val savedLoginState = sharedPref.getBoolean("is_logged_in", false)
                val savedUsername = sharedPref.getString("current_username", "") ?: ""

                var isLoggedIn by rememberSaveable { mutableStateOf(savedLoginState) }
                var currentUsername by rememberSaveable { mutableStateOf(savedUsername) }

                @SuppressLint("UseKtx")
                fun updateLoginState(loggedIn: Boolean, username: String = "") {
                    isLoggedIn = loggedIn
                    currentUsername = username
                    with(sharedPref.edit()) {
                        putBoolean("is_logged_in", loggedIn)
                        putString("current_username", username)
                        apply()
                    }
                }

                if (isLoggedIn) {
                    MainAppContent(
                        viewModel = viewModel,
                        currentUsername = currentUsername,
                        onLogout = { updateLoginState(false, "") }
                    )
                } else {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { username -> updateLoginState(true, username) }
                    )
                }
            }
        }
    }
}

// --- 2. Main Navigation ---

@Composable
fun MainAppContent(
    viewModel: FitnessViewModel,
    currentUsername: String,
    onLogout: () -> Unit
) {
    val tabs = listOf("Dashboard", "Fitness", "Nutrition", "Mindfulness")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF009688), // Teal Icon
                            selectedTextColor = Color(0xFF009688), // Teal Text
                            indicatorColor = Color(0xFFE0F2F1)     // Very light teal background
                        ),
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
                "Dashboard" -> DashboardScreen(currentUsername = currentUsername, onLogout = onLogout)
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
fun LoginScreen(
    viewModel: FitnessViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isLoginEnabled = username.isNotBlank() && password.isNotBlank()

    // 1. Use a Box with the Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LogoGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2. Logo / Title Text
            Text(
                text = if (isSignUpMode) "Create Account" else "365Fitness",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // 3. Inputs with White styling
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Login/Sign Up Button
            Button(
                onClick = {
                    if (isLoginEnabled) {
                        // Play sound effect
                        val mediaPlayer = MediaPlayer.create(context, R.raw.intro)
                        if (mediaPlayer != null) {
                            mediaPlayer.start()
                            Handler(Looper.getMainLooper()).postDelayed({
                                try {
                                    if (mediaPlayer.isPlaying) {
                                        mediaPlayer.stop()
                                    }
                                    mediaPlayer.release()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, 5000)
                        }

                        if (isSignUpMode) {
                            // Handle Sign Up
                            val newUser = User(username = username, password = password)
                            viewModel.registerUser(newUser) { success ->
                                if (success) {
                                    Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    // Switch to login mode
                                    isSignUpMode = false
                                    password = "" // Clear password field
                                } else {
                                    Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Handle Login
                            viewModel.loginUser(username, password) { success ->
                                if (success == "Success") {
                                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(username)
                                } else {
                                    Toast.makeText(context, "Invalid username or password!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                enabled = isLoginEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF009688),
                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF009688).copy(alpha = 0.5f)
                )
            ) {
                Text(if (isSignUpMode) "Sign Up" else "Login", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                isSignUpMode = !isSignUpMode
                username = ""
                password = ""
            }) {
                Text(
                    if (isSignUpMode) "Already have an account? Login" else "Don't have an account? Sign Up",
                    color = Color.White
                )
            }
        }
    }
}

// --- 5. Dashboard Screen ---

@Composable
fun DashboardScreen(currentUsername: String, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Welcome Header with Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back, $currentUsername!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688)
                )
                Text(
                    text = "Here is your daily summary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. The "Hero" Gradient Card (Steps & Calories)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(LogoGradient)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Steps
                    Column {
                        Text("STEPS", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                        Text("7,230", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text("Goal: 10,000", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    // Right Side: Calories
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CALORIES", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                        Text("450", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text("Kcal Burned", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Quick Stats Grid (Water & Sleep)
        Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Water Card
            StatCard(
                title = "Water",
                value = "1.2 L",
                icon = Icons.Outlined.LocalDrink,
                tint = Color(0xFF03A9F4),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Sleep Card
            StatCard(
                title = "Sleep",
                value = "7h 30m",
                icon = Icons.Outlined.Bedtime,
                tint = Color(0xFF673AB7),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Motivation / Quote Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFF009688)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Streak: 3 Days!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B)
                    )
                    Text(
                        text = "Keep it up, consistency is key!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF004D40)
                    )
                }
            }
        }
    }
}

// --- Helper Component for the smaller cards ---
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// --- 6. Fitness Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF009688),
                contentColor = Color.White
            )
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

// --- 7. Nutrition Screen ---
@Composable
fun NutritionScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
                        viewModel.deleteMeal(meal)
                        Toast.makeText(context, "Meal Deleted", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddMealDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF009688),
                contentColor = Color.White
            )
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

// --- 8. Mindfulness Screen ---
@Composable
fun MindfulnessScreen(viewModel: FitnessViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                    progress = {
                        if (selectedDuration > 0) {
                            1f - (timeRemaining.toFloat() / (selectedDuration * 60f))
                        } else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF009688),
                    trackColor = Color(0xFFB2DFDB)
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
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(0xFF009688)
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
@SuppressLint("DefaultLocale")
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}