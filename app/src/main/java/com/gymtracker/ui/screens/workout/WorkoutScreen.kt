package com.gymtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.RoutineExercise
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.model.WorkoutSet
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    selectedExercise: Exercise? = null,
    onExerciseHandled: () -> Unit = {},
    onNavigateToExercises: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSelectedExercise by viewModel.selectedExercise.collectAsStateWithLifecycle()
    val isAddingSet by viewModel.isAddingSet.collectAsStateWithLifecycle()
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    val isRestTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val routineExercises by viewModel.routineExercises.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()

    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Calculate elapsed minutes for active workout
    val elapsedMinutes = remember(uiState) {
        when (val state = uiState) {
            is WorkoutUiState.Active -> {
                val elapsed = System.currentTimeMillis() - state.startTime
                TimeUnit.MILLISECONDS.toMinutes(elapsed)
            }
            else -> 0L
        }
    }

    LaunchedEffect(selectedExercise) {
        if (selectedExercise != null) {
            viewModel.selectExercise(selectedExercise)
            onExerciseHandled()
        }
    }

    val isActive = uiState is WorkoutUiState.Active

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (isActive) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinimalIconButton(
                            icon = Icons.Outlined.Close,
                            onClick = { showCancelDialog = true }
                        )
                        MinimalIconButton(
                            icon = Icons.Outlined.Check,
                            onClick = { showFinishDialog = true }
                        )
                    }
                }
            }

            // Rest Timer Banner
            AnimatedVisibility(
                visible = isRestTimerRunning,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                RestTimerBanner(
                    seconds = restTimerSeconds,
                    onAddTime = { viewModel.addRestTime(30) },
                    onStop = { viewModel.stopRestTimer() }
                )
            }

            when (val state = uiState) {
                is WorkoutUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TextPrimary)
                    }
                }

                is WorkoutUiState.NoActiveWorkout -> {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
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
                                text = "No Active Workout",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start a workout from the Home screen",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is WorkoutUiState.Active -> {
                    // Stats Card
                    MinimalStatsCard(
                        elapsedMinutes = elapsedMinutes,
                        totalSets = state.totalSets,
                        totalVolume = state.totalVolume,
                        weightUnit = weightUnit,
                        routineName = state.routineName
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exercise List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Completed exercises with sets
                        items(state.exercisesWithSets, key = { it.exerciseId }) { exerciseWithSets ->
                            ExerciseSetCard(
                                exerciseName = exerciseWithSets.exerciseName,
                                exerciseId = exerciseWithSets.exerciseId,
                                sets = exerciseWithSets.sets,
                                weightUnit = weightUnit,
                                onAddSet = { viewModel.selectExerciseById(exerciseWithSets.exerciseId, exerciseWithSets.exerciseName) },
                                onDeleteSet = { viewModel.deleteSet(it) }
                            )
                        }

                        // Pending routine exercises
                        if (routineExercises.isNotEmpty()) {
                            val completedExerciseIds = state.exercisesWithSets.map { it.exerciseId }.toSet()
                            val pendingExercises = routineExercises.filter { it.exercise.id !in completedExerciseIds }

                            if (pendingExercises.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Up Next",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(pendingExercises, key = { it.exercise.id }) { routineExercise ->
                                    PendingExerciseCard(
                                        routineExercise = routineExercise,
                                        onStart = { viewModel.selectExercise(routineExercise.exercise) }
                                    )
                                }
                            }
                        }

                        // Add Exercise Button
                        item {
                            MinimalAddButton(
                                text = "Add Exercise",
                                onClick = onNavigateToExercises
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Set Bottom Sheet
    if (isAddingSet && currentSelectedExercise != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedExercise() },
            sheetState = sheetState,
            containerColor = CardBackground
        ) {
            AddSetBottomSheet(
                exerciseName = currentSelectedExercise!!.name,
                weightUnit = weightUnit,
                onSave = { weight, reps, rpe ->
                    viewModel.addSet(weight, reps, rpe)
                },
                onDismiss = { viewModel.clearSelectedExercise() }
            )
        }
    }

    // Finish Dialog
    if (showFinishDialog) {
        MinimalDialog(
            title = "Finish Workout?",
            message = "Save this workout session?",
            confirmText = "Finish",
            onConfirm = {
                viewModel.finishWorkout()
                showFinishDialog = false
            },
            onDismiss = { showFinishDialog = false }
        )
    }

    // Cancel Dialog
    if (showCancelDialog) {
        MinimalDialog(
            title = "Cancel Workout?",
            message = "All progress will be lost.",
            confirmText = "Cancel Workout",
            confirmColor = Color(0xFFFF453A),
            onConfirm = {
                viewModel.cancelWorkout()
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
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
private fun RestTimerBanner(
    seconds: Int,
    onAddTime: () -> Unit,
    onStop: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "REST",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatTime(seconds),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardBorder,
                    modifier = Modifier.clickable(onClick = onAddTime)
                ) {
                    Text(
                        "+30s",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardBorder,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onStop)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Stop",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalStatsCard(
    elapsedMinutes: Long,
    totalSets: Int,
    totalVolume: Float,
    weightUnit: WeightUnit,
    routineName: String?
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp)
    ) {
        Column {
            if (routineName != null) {
                Text(
                    text = routineName,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "$elapsedMinutes", label = "Minutes")
                StatItem(value = "$totalSets", label = "Sets")
                StatItem(value = "${totalVolume.toInt()}", label = "Volume", suffix = weightUnit.symbol)
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    suffix: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ExerciseSetCard(
    exerciseName: String,
    exerciseId: Long,
    sets: List<WorkoutSet>,
    weightUnit: WeightUnit,
    onAddSet: () -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardBorder,
                    modifier = Modifier.clickable(onClick = onAddSet)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Set",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            if (sets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SET", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(40.dp))
                    Text(weightUnit.symbol.uppercase(), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("REPS", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.width(36.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                sets.forEachIndexed { index, set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            "${set.weight}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "${set.reps}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { onDeleteSet(set) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingExerciseCard(
    routineExercise: RoutineExercise,
    onStart: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onStart)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CardBorder,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routineExercise.exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${routineExercise.targetSets} sets · ${routineExercise.targetReps}",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MinimalAddButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Surface(
        shape = shape,
        color = CardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun AddSetBottomSheet(
    exerciseName: String,
    weightUnit: WeightUnit,
    onSave: (Float, Int, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = exerciseName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MinimalTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "Weight (${weightUnit.symbol})",
                modifier = Modifier.weight(1f)
            )
            MinimalTextField(
                value = reps,
                onValueChange = { reps = it },
                label = "Reps",
                modifier = Modifier.weight(1f)
            )
            MinimalTextField(
                value = rpe,
                onValueChange = { rpe = it },
                label = "RPE",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
            ) {
                Text(
                    "Cancel",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBorder,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val w = weight.toFloatOrNull() ?: 0f
                        val r = reps.toIntOrNull() ?: 0
                        val rpeVal = rpe.toIntOrNull()
                        onSave(w, r, rpeVal)
                    }
            ) {
                Text(
                    "Save Set",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CardBackground)
                .border(1.dp, CardBorder, shape)
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(TextPrimary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MinimalDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color = TextPrimary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
