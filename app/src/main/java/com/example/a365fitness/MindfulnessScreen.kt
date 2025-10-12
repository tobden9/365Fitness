package com.example.a365fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MindfulnessScreen(modifier: Modifier = Modifier) {
    var selectedDuration by remember { mutableStateOf(5) }
    var timerRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(selectedDuration * 60L) }
    var selectedMeditationType by remember { mutableStateOf("Breathing") }

    var meditationHistory by remember {
        mutableStateOf(
            listOf(
                MeditationSession("Breathing", "5", "Today, 09:30 AM"),
                MeditationSession("Body Scan", "10", "Yesterday, 08:15 PM")
            )
        )
    }

    val currentDate = remember {
        java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    val currentTime = remember {
        java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    val meditationTypes = listOf("Breathing", "Body Scan", "Loving-Kindness", "Visualization")

    fun saveMeditationSession() {
        val completedMinutes = selectedDuration - (timeRemaining / 60)
        if (completedMinutes > 0) {
            val newSession = MeditationSession(
                selectedMeditationType,
                "$completedMinutes",
                "Today, ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
            )
            meditationHistory = listOf(newSession) + meditationHistory
        }
    }

    LaunchedEffect(timerRunning) {
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

                Text(
                    text = "Completed: ${selectedDuration - (timeRemaining / 60)} min",
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
            modifier = Modifier.padding(bottom = 16.dp)
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

        // Timer Controls - TEXT ONLY (no icons or emojis)
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
            Column {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(meditationHistory) { session ->
                        MeditationHistoryItem(session = session)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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

data class MeditationSession(
    val type: String,
    val duration: String,
    val date: String
)

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
                Column {
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

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

