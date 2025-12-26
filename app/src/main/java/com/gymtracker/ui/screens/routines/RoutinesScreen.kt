package com.gymtracker.ui.screens.routines

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.domain.model.Routine
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBackground
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoutinesScreen(
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onStartRoutine: (Long) -> Unit,
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

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
                    text = "Routines",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MinimalIconButton(
                        icon = Icons.Outlined.Settings,
                        onClick = { }
                    )
                    MinimalIconButton(
                        icon = Icons.Outlined.Add,
                        onClick = onCreateRoutine
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TextPrimary)
                    }
                }
                uiState.routines.isEmpty() -> {
                    EmptyRoutinesContent(onCreateRoutine = onCreateRoutine)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.routines, key = { it.id }) { routine ->
                            MinimalRoutineCard(
                                routine = routine,
                                onStart = { onStartRoutine(routine.id) },
                                onEdit = { onEditRoutine(routine.id) },
                                onDelete = { routineToDelete = routine }
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

    // Delete confirmation dialog
    routineToDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            containerColor = CardBackground,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Delete Routine?") },
            text = { Text("Are you sure you want to delete \"${routine.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoutine(routine.id)
                        routineToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF453A))
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
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
private fun EmptyRoutinesContent(
    onCreateRoutine: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Routines Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create workout routines to quickly start your favorite workouts",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CardBackground,
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onCreateRoutine)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Routine",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MinimalRoutineCard(
    routine: Routine,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, cardShape)
            .clickable(onClick = onEdit)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (routine.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = routine.description,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Play button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBorder,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onStart)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Start routine",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise count and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${routine.exerciseCount} exercises",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = " · ",
                        fontSize = 13.sp,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = dateFormat.format(Date(routine.createdAt)),
                        fontSize = 13.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
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
