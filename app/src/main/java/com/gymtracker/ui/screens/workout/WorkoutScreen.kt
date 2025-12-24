package com.gymtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Handle exercise selected from picker
    LaunchedEffect(selectedExercise) {
        if (selectedExercise != null) {
            viewModel.selectExercise(selectedExercise)
            onExerciseHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState is WorkoutUiState.Active) {
                        IconButton(onClick = { showCancelDialog = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel workout")
                        }
                        IconButton(onClick = { showFinishDialog = true }) {
                            Icon(Icons.Default.Done, contentDescription = "Finish workout")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is WorkoutUiState.Active) {
                FloatingActionButton(
                    onClick = onNavigateToExercises,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add exercise")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is WorkoutUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is WorkoutUiState.NoActiveWorkout -> {
                NoActiveWorkoutContent(
                    onStartWorkout = viewModel::startNewWorkout,
                    modifier = Modifier.padding(paddingValues)
                )
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
                    modifier = Modifier.padding(paddingValues)
                )
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
        FinishWorkoutDialog(
            onDismiss = { showFinishDialog = false },
            onConfirm = { notes ->
                viewModel.finishWorkout(notes)
                showFinishDialog = false
            }
        )
    }

    // Cancel Workout Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Workout?") },
            text = { Text("This will delete all logged sets. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelWorkout()
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancel Workout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Going")
                }
            }
        )
    }
}

@Composable
private fun NoActiveWorkoutContent(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Active Workout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start a new workout to begin logging your exercises",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Workout")
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
    modifier: Modifier = Modifier
) {
    // Track which exercises have been logged
    val loggedExerciseIds = state.exercisesWithSets.map { it.exerciseId }.toSet()

    Column(modifier = modifier.fillMaxSize()) {
        // Rest Timer Banner
        AnimatedVisibility(
            visible = isRestTimerRunning,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            RestTimerBanner(
                seconds = restTimerSeconds,
                onAddTime = { onAddRestTime(30) },
                onStop = onStopTimer
            )
        }

        // Workout Stats Card
        WorkoutStatsCard(
            startTime = state.startTime,
            totalSets = state.totalSets,
            totalVolume = state.totalVolume,
            routineName = state.routineName,
            modifier = Modifier.padding(16.dp)
        )

        // Exercise List
        if (state.exercisesWithSets.isEmpty() && routineExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap + to add your first exercise",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show logged exercises first
                items(state.exercisesWithSets, key = { it.exerciseId }) { exerciseWithSets ->
                    ExerciseSetCard(
                        exerciseWithSets = exerciseWithSets,
                        routineExercise = routineExercises.find { it.exercise.id == exerciseWithSets.exerciseId },
                        onDeleteSet = onDeleteSet,
                        onAddSet = { onExerciseClick(exerciseWithSets.sets.first().let {
                            Exercise(
                                id = exerciseWithSets.exerciseId,
                                name = exerciseWithSets.exerciseName,
                                category = com.gymtracker.domain.model.ExerciseCategory.PUSH,
                                muscleGroups = emptyList(),
                                equipment = com.gymtracker.domain.model.Equipment.OTHER
                            )
                        }) }
                    )
                }

                // Show pending routine exercises (not yet logged)
                val pendingExercises = routineExercises.filter { it.exercise.id !in loggedExerciseIds }
                if (pendingExercises.isNotEmpty()) {
                    item {
                        Text(
                            text = "Remaining Exercises",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pendingExercises, key = { it.exercise.id }) { routineExercise ->
                        PendingExerciseCard(
                            routineExercise = routineExercise,
                            onClick = { onExerciseClick(routineExercise.exercise) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RestTimerBanner(
    seconds: Int,
    onAddTime: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTime(seconds),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Row {
                FilledTonalButton(onClick = onAddTime) {
                    Text("+30s")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(onClick = onStop) {
                    Icon(Icons.Default.Pause, contentDescription = "Stop timer")
                }
            }
        }
    }
}

@Composable
private fun WorkoutStatsCard(
    startTime: Long,
    totalSets: Int,
    totalVolume: Float,
    routineName: String? = null,
    modifier: Modifier = Modifier
) {
    val elapsedMinutes = remember(startTime) {
        TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime).toInt()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (routineName != null) {
                Text(
                    text = routineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "$elapsedMinutes", label = "Minutes")
                StatItem(value = "$totalSets", label = "Sets")
                StatItem(value = "${totalVolume.toInt()}", label = "Volume (kg)")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseSetCard(
    exerciseWithSets: ExerciseWithSets,
    routineExercise: RoutineExercise? = null,
    onDeleteSet: (WorkoutSet) -> Unit,
    onAddSet: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseWithSets.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (routineExercise != null) {
                        Text(
                            text = "Target: ${routineExercise.targetSets} sets x ${routineExercise.targetReps} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onAddSet,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add set",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SET",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "KG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "REPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(40.dp))
            }

            exerciseWithSets.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set number circle
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = "${set.weight}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${set.reps}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { onDeleteSet(set) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete set",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingExerciseCard(
    routineExercise: RoutineExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routineExercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${routineExercise.targetSets} sets x ${routineExercise.targetReps} reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Start exercise",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start")
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

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add Set",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = rpe,
                onValueChange = { rpe = it },
                label = { Text("RPE (optional, 1-10)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val w = weight.toFloatOrNull() ?: return@Button
                    val r = reps.toIntOrNull() ?: return@Button
                    val rpeValue = rpe.toIntOrNull()?.coerceIn(1, 10)
                    onAddSet(w, r, rpeValue)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = weight.toFloatOrNull() != null && reps.toIntOrNull() != null
            ) {
                Text("Add Set")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (notes: String?) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Workout?") },
        text = {
            Column {
                Text("Add any notes about your workout (optional):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("How did it go?") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(notes.ifBlank { null }) }) {
                Text("Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
