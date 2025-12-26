package com.gymtracker.ui.screens.workoutdetail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.WeightUnit
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
fun WorkoutDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (val state = uiState) {
            is WorkoutDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TextPrimary)
                }
            }
            is WorkoutDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = TextSecondary
                    )
                }
            }
            is WorkoutDetailUiState.Success -> {
                WorkoutDetailContent(
                    state = state,
                    weightUnit = weightUnit,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun WorkoutDetailContent(
    state: WorkoutDetailUiState.Success,
    weightUnit: WeightUnit,
    onNavigateBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val durationMinutes = state.workout.endTime?.let {
        TimeUnit.MILLISECONDS.toMinutes(it - state.workout.startTime)
    } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = dateFormat.format(Date(state.workout.startTime)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = timeFormat.format(Date(state.workout.startTime)),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Duration",
                    value = "${durationMinutes}min",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Volume",
                    value = "${state.totalVolume.toInt()} ${weightUnit.symbol}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Sets",
                    value = "${state.totalSets}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Exercises header
        if (state.exercises.isNotEmpty()) {
            item {
                Text(
                    text = "Exercises",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state.exercises) { exercise ->
                ExerciseDetailCard(
                    exercise = exercise,
                    weightUnit = weightUnit
                )
            }
        }

        // Notes
        if (!state.workout.notes.isNullOrEmpty()) {
            item {
                Text(
                    text = "Notes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                DetailCard {
                    Text(
                        text = state.workout.notes,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ExerciseDetailCard(
    exercise: ExerciseDetail,
    weightUnit: WeightUnit
) {
    DetailCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.exerciseName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SET",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = weightUnit.symbol.uppercase(),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "REPS",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sets
            exercise.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = "${set.weight}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${set.reps}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
    ) {
        content()
    }
}
