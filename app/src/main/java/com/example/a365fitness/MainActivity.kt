package com.example.a365fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun FitnessScreen(modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Text("Workout Log", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Exercise: Running")
                Text("Duration: 30 mins")
                Text("Intensity: Moderate")
            }
        }
        Button(onClick = { /* Add new workout */ }) {
            Text("Add New Workout")
        }
    }
}

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    var meal by remember { mutableStateOf("") }
    Column(modifier.padding(16.dp)) {
        Text("Log Your Meal", style = MaterialTheme.typography.headlineSmall)
        TextField(
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
