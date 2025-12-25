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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Brush
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
import com.gymtracker.domain.model.WorkoutSet
import com.gymtracker.ui.components.GlassCard
import com.gymtracker.ui.components.IconBadge
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.Primary
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

    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedExercise) {
        if (selectedExercise != null) {
            viewModel.selectExercise(selectedExercise)
            onExerciseHandled()
        }
    }

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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (uiState is WorkoutUiState.Active) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Cancel button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2A2555),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { showCancelDialog = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        // Finish button
                        Surface(
                            shape = CircleShape,
                            color = Primary,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { showFinishDialog = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Done,
                                    contentDescription = "Finish",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            when (val state = uiState) {
                is WorkoutUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is WorkoutUiState.NoActiveWorkout -> {
                    NoActiveWorkoutContent(onStartWorkout = viewModel::startNewWorkout)
                }

                is WorkoutUiState.Active -> {
                    ActiveWorkoutContent(
                        state = state,
                        routineExercises = routineExercises,
                        restTimerSeconds = restTimerSeconds,
                        isRestTimerRunning = isRestTimerRunning,
                        onAddRestTime = viewModel::addRestTime,
                        onStopTimer = viewModel::stopRestTimer,
                        onDeleteSet = viewModel::deleteSet,
                        onExerciseClick = viewModel::selectExercise,
                        onNavigateToExercises = onNavigateToExercises
                    )
                }
            }
        }
    }

    // Add Set Bottom Sheet
    if (isAddingSet && currentSelectedExercise != null) {
        AddSetBottomSheet(
            exercise = currentSelectedExercise!!,
            onDismiss = viewModel::clearSelectedExercise,
            onAddSet = { weight, reps, rpe ->
                viewModel.addSet(weight, reps, rpe)
            }
        )
    }

    // Finish Workout Dialog
    if (showFinishDialog) {
        PremiumDialog(
            title = "Finish Workout?",
            content = {
                var notes by remember { mutableStateOf("") }
                Column {
                    Text(
                        "Add notes (optional):",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PremiumTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "How did it go?"
                    )
                }
            },
            onDismiss = { showFinishDialog = false },
            onConfirm = {
                viewModel.finishWorkout(null)
                showFinishDialog = false
            },
            confirmText = "Finish",
            dismissText = "Cancel"
        )
    }

    // Cancel Workout Dialog
    if (showCancelDialog) {
        PremiumDialog(
            title = "Cancel Workout?",
            content = {
                Text(
                    "This will delete all logged sets. Are you sure?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            },
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                viewModel.cancelWorkout()
                showCancelDialog = false
            },
            confirmText = "Cancel Workout",
            dismissText = "Keep Going",
            isDestructive = true
        )
    }
}

@Composable
private fun NoActiveWorkoutContent(onStartWorkout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Active Workout",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start a new workout to begin logging your exercises",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Primary,
            modifier = Modifier.clickable(onClick = onStartWorkout)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Start Workout",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveWorkoutContent(
    state: WorkoutUiState.Active,
    routineExercises: List<RoutineExercise>,
    restTimerSeconds: Int,
    isRestTimerRunning: Boolean,
    onAddRestTime: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onNavigateToExercises: () -> Unit
) {
    val loggedExerciseIds = state.exercisesWithSets.map { it.exerciseId }.toSet()

    Column(modifier = Modifier.fillMaxSize()) {
        // Rest Timer Banner
        AnimatedVisibility(
            visible = isRestTimerRunning,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            PremiumRestTimerBanner(
                seconds = restTimerSeconds,
                onAddTime = { onAddRestTime(30) },
                onStop = onStopTimer
            )
        }

        // Workout Stats Card
        PremiumStatsCard(
            startTime = state.startTime,
            totalSets = state.totalSets,
            totalVolume = state.totalVolume,
            routineName = state.routineName,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise List
        if (state.exercisesWithSets.isEmpty() && routineExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tap + to add your first exercise",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = CircleShape,
                        color = Primary,
                        modifier = Modifier
                            .size(64.dp)
                            .clickable(onClick = onNavigateToExercises)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add exercise",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show logged exercises first
                items(state.exercisesWithSets, key = { it.exerciseId }) { exerciseWithSets ->
                    PremiumExerciseSetCard(
                        exerciseWithSets = exerciseWithSets,
                        routineExercise = routineExercises.find { it.exercise.id == exerciseWithSets.exerciseId },
                        onDeleteSet = onDeleteSet,
                        onAddSet = {
                            onExerciseClick(
                                Exercise(
                                    id = exerciseWithSets.exerciseId,
                                    name = exerciseWithSets.exerciseName,
                                    category = com.gymtracker.domain.model.ExerciseCategory.PUSH,
                                    muscleGroups = emptyList(),
                                    equipment = com.gymtracker.domain.model.Equipment.OTHER
                                )
                            )
                        }
                    )
                }

                // Show pending routine exercises
                val pendingExercises = routineExercises.filter { it.exercise.id !in loggedExerciseIds }
                if (pendingExercises.isNotEmpty()) {
                    item {
                        Text(
                            text = "Remaining Exercises",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pendingExercises, key = { it.exercise.id }) { routineExercise ->
                        PremiumPendingExerciseCard(
                            routineExercise = routineExercise,
                            onClick = { onExerciseClick(routineExercise.exercise) }
                        )
                    }
                }

                // Add exercise button
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF2A2555),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToExercises)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add Exercise",
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PremiumRestTimerBanner(
    seconds: Int,
    onAddTime: () -> Unit,
    onStop: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val progress = (seconds % 90) / 90f // Circular progress based on typical rest time

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E1A3D),
                        Color(0xFF2A2255)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4A4570).copy(alpha = 0.6f),
                        Color(0xFF6C5CE7).copy(alpha = 0.4f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer display with circular progress
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    // Background ring
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(56.dp),
                        color = Color(0xFF3A3560),
                        strokeWidth = 4.dp
                    )
                    // Progress ring
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(56.dp),
                        color = Color(0xFF4ECDC4),
                        strokeWidth = 4.dp
                    )
                    // Timer icon
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF4ECDC4),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "REST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4ECDC4),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatTime(seconds),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // +30s button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF4ECDC4).copy(alpha = 0.15f),
                    modifier = Modifier.clickable(onClick = onAddTime)
                ) {
                    Text(
                        "+30s",
                        color = Color(0xFF4ECDC4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                // Stop button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onStop)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumStatsCard(
    startTime: Long,
    totalSets: Int,
    totalVolume: Float,
    routineName: String?,
    modifier: Modifier = Modifier
) {
    val elapsedMinutes = remember(startTime) {
        TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime).toInt()
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column {
            if (routineName != null) {
                Text(
                    text = routineName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9D8DF1),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PremiumStatItem(value = "$elapsedMinutes", label = "Minutes", color = Primary)
                PremiumStatItem(value = "$totalSets", label = "Sets", color = Color(0xFF9D8DF1))
                PremiumStatItem(value = "${totalVolume.toInt()}", label = "Volume", suffix = "kg", color = Color(0xFF4ECDC4))
            }
        }
    }
}

@Composable
private fun PremiumStatItem(
    value: String,
    label: String,
    color: Color,
    suffix: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PremiumExerciseSetCard(
    exerciseWithSets: ExerciseWithSets,
    routineExercise: RoutineExercise?,
    onDeleteSet: (WorkoutSet) -> Unit,
    onAddSet: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1A3D), Color(0xFF151030))
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF3A3560).copy(alpha = 0.5f),
                shape = cardShape
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseWithSets.exerciseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (routineExercise != null) {
                        Text(
                            text = "Target: ${routineExercise.targetSets} x ${routineExercise.targetReps}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Primary,
                    modifier = Modifier.clickable(onClick = onAddSet)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("SET", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f))
                Text("KG", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("REPS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(36.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            exerciseWithSets.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("${set.weight}", fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("${set.reps}", fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton(onClick = { onDeleteSet(set) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPendingExerciseCard(
    routineExercise: RoutineExercise,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF1A1535).copy(alpha = 0.5f))
            .border(1.dp, Color(0xFF3A3560).copy(alpha = 0.3f), cardShape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            IconBadge(
                icon = Icons.Default.FitnessCenter,
                backgroundColor = Color(0xFF4ECDC4).copy(alpha = 0.15f),
                iconColor = Color(0xFF4ECDC4),
                size = 44.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(routineExercise.exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text("${routineExercise.targetSets} x ${routineExercise.targetReps}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF4ECDC4),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSetBottomSheet(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onAddSet: (weight: Float, reps: Int, rpe: Int?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color(0xFF1A1432)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Add Set", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(exercise.name, fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Weight (kg)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumNumberField(value = weight, onValueChange = { weight = it })
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reps", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumNumberField(value = reps, onValueChange = { reps = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("RPE (optional)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            PremiumNumberField(value = rpe, onValueChange = { rpe = it }, placeholder = "1-10")

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (weight.toFloatOrNull() != null && reps.toIntOrNull() != null) Primary else Color(0xFF2A2555),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = weight.toFloatOrNull() != null && reps.toIntOrNull() != null) {
                        val w = weight.toFloatOrNull() ?: return@clickable
                        val r = reps.toIntOrNull() ?: return@clickable
                        val rpeValue = rpe.toIntOrNull()?.coerceIn(1, 10)
                        onAddSet(w, r, rpeValue)
                    }
            ) {
                Text(
                    "Add Set",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PremiumNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF252047))
            .border(1.dp, Color(0xFF3A3560).copy(alpha = 0.5f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            singleLine = true,
            cursorBrush = SolidColor(Primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF252047))
            .border(1.dp, Color(0xFF3A3560).copy(alpha = 0.5f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            cursorBrush = SolidColor(Primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumDialog(
    title: String,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String,
    dismissText: String,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1A3D),
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { content() },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (isDestructive) Primary else Color(0xFF4ECDC4))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
