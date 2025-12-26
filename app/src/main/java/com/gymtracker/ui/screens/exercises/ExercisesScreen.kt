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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary

@Composable
fun ExercisesScreen(
    onExerciseClick: (Exercise) -> Unit = {},
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val totalExercises = uiState.exercises.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercises",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (totalExercises > 0) {
                    Text(
                        text = "$totalExercises",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            // Search bar
            MinimalSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category filter chips
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelect = viewModel::onCategorySelect,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TextPrimary)
                    }
                }
                uiState.exercises.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No exercises found",
                                fontSize = 16.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    ExercisesList(
                        groupedExercises = uiState.groupedExercises,
                        onExerciseClick = onExerciseClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search exercises...",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterChips(
    selectedCategory: ExerciseCategory?,
    onCategorySelect: (ExerciseCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MinimalChip(
            text = "All",
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) }
        )
        ExerciseCategory.entries.forEach { category ->
            MinimalChip(
                text = category.displayName,
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) }
            )
        }
    }
}

@Composable
private fun MinimalChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val backgroundColor = if (selected) CardBorder else CardBackground
    val textColor = if (selected) TextPrimary else TextSecondary

    Surface(
        shape = shape,
        color = backgroundColor,
        modifier = Modifier
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ExercisesList(
    groupedExercises: Map<ExerciseCategory, List<Exercise>>,
    onExerciseClick: (Exercise) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedExercises.forEach { (category, exercises) ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${exercises.size}",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }
            items(exercises, key = { it.id }) { exercise ->
                MinimalExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }
        }

        // Bottom spacing for nav bar
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun MinimalExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(12.dp)
    val categoryIcon = getCategoryIcon(exercise.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CardBorder,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary muscle
                Text(
                    text = exercise.muscleGroups.firstOrNull() ?: "",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (exercise.muscleGroups.size > 1) {
                    Text(
                        text = "+${exercise.muscleGroups.size - 1}",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "·",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.4f)
                )
                Text(
                    text = exercise.equipment.displayName,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun getCategoryIcon(category: ExerciseCategory): ImageVector {
    return when (category) {
        ExerciseCategory.PUSH -> Icons.Outlined.FitnessCenter
        ExerciseCategory.PULL -> Icons.Outlined.FitnessCenter
        ExerciseCategory.LEGS -> Icons.Outlined.SportsGymnastics
        ExerciseCategory.CORE -> Icons.Outlined.SelfImprovement
        ExerciseCategory.CARDIO -> Icons.AutoMirrored.Outlined.DirectionsRun
    }
}
