package com.gymtracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Workout
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onContinueWorkout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onWorkoutClick: (Long) -> Unit,
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
                CircularProgressIndicator(color = TextPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_title),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        MinimalIconButton(
                            icon = Icons.Outlined.Settings,
                            onClick = onNavigateToSettings
                        )
                    }
                }

                // Active Workout or Quick Start Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.hasActiveWorkout) {
                            // Continue Session Card
                            MinimalCard(
                                modifier = Modifier.weight(1f),
                                onClick = onContinueWorkout
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Progress ring placeholder
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(2.dp, TextSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.PlayArrow,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text(
                                        text = stringResource(R.string.home_continue_session),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.home_tap_to_resume),
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            // Start Workout Card
                            MinimalCard(
                                modifier = Modifier.weight(1f),
                                onClick = onStartWorkout
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(2.dp, TextSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            fontSize = 24.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text(
                                        text = stringResource(R.string.home_start_workout),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.home_new_session),
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Stats Card
                        MinimalCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${uiState.workoutsThisWeek}",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = stringResource(R.string.home_workouts),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.home_this_week),
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Volume Stats Card
                item {
                    MinimalCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.home_volume_lifted),
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = stringResource(R.string.home_last_7_days),
                                    fontSize = 12.sp,
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = "${uiState.weeklyVolume.toInt()} ${uiState.weightUnit.symbol}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Recent Workouts
                if (uiState.recentWorkouts.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.home_recent),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.recentWorkouts.take(5)) { workout ->
                        MinimalWorkoutItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) }
                        )
                    }
                }

                // Bottom spacing for nav bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun MinimalIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        modifier = Modifier
            .size(44.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MinimalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        content()
    }
}

@Composable
private fun MinimalWorkoutItem(
    workout: Workout,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val durationMinutes = workout.endTime?.let {
        TimeUnit.MILLISECONDS.toMinutes(it - workout.startTime)
    } ?: TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - workout.startTime)

    MinimalCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TextSecondary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = dateFormat.format(Date(workout.startTime)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.home_minutes, durationMinutes),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = stringResource(R.string.home_min, durationMinutes),
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}
