package com.gymtracker.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.repository.ExerciseHistoryItem
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (val state = uiState) {
            is ExerciseDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TextPrimary)
                }
            }
            is ExerciseDetailUiState.Error -> {
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
            is ExerciseDetailUiState.Success -> {
                ExerciseDetailContent(
                    state = state,
                    weightUnit = weightUnit,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetailContent(
    state: ExerciseDetailUiState.Success,
    weightUnit: WeightUnit,
    onNavigateBack: () -> Unit
) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.exercise.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = state.exercise.category.displayName,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Tags: muscle groups & equipment
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.exercise.muscleGroups.forEach { muscle ->
                    InfoTag(text = muscle)
                }
                InfoTag(
                    text = state.exercise.equipment.displayName,
                    icon = Icons.Outlined.FitnessCenter
                )
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Outlined.EmojiEvents,
                    label = "PR",
                    value = if (state.personalRecord != null) {
                        "${state.personalRecord.toInt()} ${weightUnit.symbol}"
                    } else {
                        "-"
                    },
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.Timeline,
                    label = "Workouts",
                    value = "${state.workoutCount}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.Scale,
                    label = "Volume",
                    value = formatVolume(state.totalVolume, weightUnit),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // History section
        if (state.history.isNotEmpty()) {
            item {
                Text(
                    text = "History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state.history) { historyItem ->
                HistoryCard(
                    historyItem = historyItem,
                    weightUnit = weightUnit
                )
            }
        } else {
            item {
                EmptyHistoryCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoTag(
    text: String,
    icon: ImageVector? = null
) {
    val shape = RoundedCornerShape(8.dp)

    Surface(
        shape = shape,
        color = CardBackground,
        modifier = Modifier.border(1.dp, CardBorder, shape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
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
private fun HistoryCard(
    historyItem: ExerciseHistoryItem,
    weightUnit: WeightUnit
) {
    val shape = RoundedCornerShape(16.dp)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateFormat.format(Date(historyItem.workoutDate)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sets summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                historyItem.sets.forEachIndexed { index, set ->
                    if (index < 5) { // Show max 5 sets inline
                        SetBadge(
                            weight = set.weight,
                            reps = set.reps,
                            weightUnit = weightUnit
                        )
                    }
                }
                if (historyItem.sets.size > 5) {
                    Text(
                        text = "+${historyItem.sets.size - 5}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetBadge(
    weight: Float,
    reps: Int,
    weightUnit: WeightUnit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${weight.toInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = "${reps}r",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Timeline,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No history yet",
                fontSize = 16.sp,
                color = TextSecondary
            )
            Text(
                text = "Complete a workout with this exercise",
                fontSize = 13.sp,
                color = TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatVolume(volume: Float, weightUnit: WeightUnit): String {
    return when {
        volume >= 1_000_000 -> "${(volume / 1_000_000).toInt()}M ${weightUnit.symbol}"
        volume >= 1_000 -> "${(volume / 1_000).toInt()}K ${weightUnit.symbol}"
        else -> "${volume.toInt()} ${weightUnit.symbol}"
    }
}
