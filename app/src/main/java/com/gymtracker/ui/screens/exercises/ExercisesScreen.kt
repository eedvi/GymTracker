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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import com.gymtracker.ui.components.IconBadge
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.Primary

@Composable
fun ExercisesScreen(
    onExerciseClick: (Exercise) -> Unit = {},
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = "Exercises",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp)
            )

            // Search bar
            PremiumSearchBar(
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
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.exercises.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No exercises found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.6f)
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
private fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF1E1A3D))
            .border(
                width = 1.dp,
                color = Color(0xFF3A3560).copy(alpha = 0.5f),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search exercises...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Primary),
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
        PremiumChip(
            text = "All",
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) }
        )
        ExerciseCategory.entries.forEach { category ->
            PremiumChip(
                text = category.displayName,
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) }
            )
        }
    }
}

@Composable
private fun PremiumChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = if (selected) Primary else Color(0xFF2A2555)
    val textColor = if (selected) Color.White else Color.White.copy(alpha = 0.7f)

    Surface(
        shape = shape,
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedExercises.forEach { (category, exercises) ->
            item {
                Text(
                    text = category.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(exercises, key = { it.id }) { exercise ->
                PremiumExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }
        }

        // Bottom spacing for nav bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PremiumExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E1A3D),
                        Color(0xFF1A1535)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF3A3560).copy(alpha = 0.5f),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = Icons.Default.FitnessCenter,
            backgroundColor = Primary.copy(alpha = 0.15f),
            iconColor = Primary,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.muscleGroups.joinToString(", "),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = exercise.equipment.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4ECDC4)
            )
        }
    }
}
