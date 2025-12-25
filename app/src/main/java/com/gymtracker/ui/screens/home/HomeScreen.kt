package com.gymtracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Workout
import com.gymtracker.ui.components.GlassCard
import com.gymtracker.ui.components.GradientCard
import com.gymtracker.ui.components.IconBadge
import com.gymtracker.ui.components.PremiumListItem
import com.gymtracker.ui.components.SectionHeader
import com.gymtracker.ui.components.StatDisplay
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onContinueWorkout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "GymTracker",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Hero Card
                item {
                    HeroCard(
                        hasActiveWorkout = uiState.hasActiveWorkout,
                        onStartWorkout = onStartWorkout,
                        onContinueWorkout = onContinueWorkout
                    )
                }

                // Stats Card
                item {
                    StatsCard(
                        workoutsThisWeek = uiState.workoutsThisWeek,
                        totalWorkouts = uiState.recentWorkouts.size
                    )
                }

                // Recent Workouts Section
                if (uiState.recentWorkouts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Recent Workouts",
                            action = "See all",
                            onActionClick = { /* TODO */ },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.recentWorkouts.take(5), key = { it.id }) { workout ->
                        WorkoutHistoryItem(workout = workout)
                    }
                } else {
                    item {
                        EmptyStateCard()
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    hasActiveWorkout: Boolean,
    onStartWorkout: () -> Unit,
    onContinueWorkout: () -> Unit
) {
    GradientCard(
        gradientColors = if (hasActiveWorkout) {
            listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
        } else {
            listOf(Color(0xFF6C5CE7), Color(0xFF8E7CF3))
        },
        onClick = if (hasActiveWorkout) onContinueWorkout else onStartWorkout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = if (hasActiveWorkout) "Workout Active" else "Ready to Train?",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasActiveWorkout) "Continue Session" else "Start Workout",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (hasActiveWorkout) Icons.Default.PlayArrow else Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress indicator or CTA
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasActiveWorkout) "Tap to continue" else "Tap to begin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun StatsCard(
    workoutsThisWeek: Int,
    totalWorkouts: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "This Week",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatDisplay(
                    value = "$workoutsThisWeek",
                    label = "Workouts",
                    valueColor = Primary
                )
                StatDisplay(
                    value = "0",
                    label = "Sets",
                    valueColor = Color(0xFF9D8DF1)
                )
                StatDisplay(
                    value = "0",
                    label = "Volume",
                    suffix = "kg",
                    valueColor = Color(0xFF4ECDC4)
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(workout: Workout) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val duration = workout.endTime?.let { end ->
        val durationMs = end - workout.startTime
        TimeUnit.MILLISECONDS.toMinutes(durationMs).toInt()
    } ?: 0

    PremiumListItem(
        title = dateFormat.format(Date(workout.startTime)),
        subtitle = workout.notes ?: "${duration} minutes",
        leadingIcon = {
            IconBadge(
                icon = Icons.Default.FitnessCenter,
                backgroundColor = Primary.copy(alpha = 0.15f),
                iconColor = Primary
            )
        },
        trailingContent = {
            Text(
                text = "${duration}min",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    )
}

@Composable
private fun EmptyStateCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No workouts yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start your first workout to track your progress",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
